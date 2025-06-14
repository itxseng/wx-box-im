package com.bx.implatform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.imcommon.model.IMSystemMessage;
import com.bx.imcommon.util.JwtUtil;
import com.bx.implatform.config.props.JwtProperties;
import com.bx.implatform.config.props.NotifyProperties;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.dto.*;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.entity.User;
import com.bx.implatform.enums.*;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.UserMapper;
import com.bx.implatform.service.*;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.util.RegexUtil;
import com.bx.implatform.util.SensitiveFilterUtil;
import com.bx.implatform.vo.LoginVO;
import com.bx.implatform.vo.OnlineTerminalVO;
import com.bx.implatform.vo.SystemMessageVO;
import com.bx.implatform.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final GroupMemberService groupMemberService;
    private final UserBlacklistService userBlacklistService;
    private final FriendService friendService;
    private final JwtProperties jwtProps;
    private final NotifyProperties notifyProps;
    private final IMClient imClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CaptchaService captchaService;
    private final SensitiveFilterUtil sensitiveFilterUtil;
    @Override
    public LoginVO login(LoginDTO dto) {
        User user = this.findUserByLoginName(dto.getUserName());
        if (Objects.isNull(user)) {
            throw new GlobalException("用户不存在");
        }
        if (user.getStatus().equals(UserStatus.UN_REG.getValue())) {
            throw new GlobalException("您的账号已注销");
        }
        if (user.getIsBanned()) {
            String tip = String.format("您的账号因'%s'已被管理员封禁,请联系客服!", user.getReason());
            throw new GlobalException(tip);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new GlobalException(ResultCode.PASSWOR_ERROR);
        }
        // 更新用户登录时间
        user.setLastLoginTime(new Date());
        this.updateById(user);
        // 生成token
        UserSession session = BeanUtils.copyProperties(user, UserSession.class);
        session.setUserId(user.getId());
        session.setTerminal(dto.getTerminal());
        String strJson = JSON.toJSONString(session);
        String accessToken =
            JwtUtil.sign(user.getId(), strJson, jwtProps.getAccessTokenExpireIn(), jwtProps.getAccessTokenSecret());
        String refreshToken =
            JwtUtil.sign(user.getId(), strJson, jwtProps.getRefreshTokenExpireIn(), jwtProps.getRefreshTokenSecret());
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProps.getAccessTokenExpireIn());
        vo.setRefreshToken(refreshToken);
        vo.setRefreshTokenExpiresIn(jwtProps.getRefreshTokenExpireIn());
        return vo;
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        //验证 token
        if (!JwtUtil.checkSign(refreshToken, jwtProps.getRefreshTokenSecret())) {
            throw new GlobalException("您的登录信息已过期，请重新登录");
        }
        String strJson = JwtUtil.getInfo(refreshToken);
        Long userId = JwtUtil.getUserId(refreshToken);
        User user = this.getById(userId);
        if (Objects.isNull(user)) {
            throw new GlobalException("用户不存在");
        }
        if (user.getIsBanned()) {
            String tip = String.format("您的账号因'%s'被管理员封禁,请联系客服!", user.getReason());
            throw new GlobalException(tip);
        }
        // 更新用户登录时间
        user.setLastLoginTime(new Date());
        this.updateById(user);
        String accessToken =
            JwtUtil.sign(userId, strJson, jwtProps.getAccessTokenExpireIn(), jwtProps.getAccessTokenSecret());
        String newRefreshToken =
            JwtUtil.sign(userId, strJson, jwtProps.getRefreshTokenExpireIn(), jwtProps.getRefreshTokenSecret());
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProps.getAccessTokenExpireIn());
        vo.setRefreshToken(newRefreshToken);
        vo.setRefreshTokenExpiresIn(jwtProps.getRefreshTokenExpireIn());
        return vo;
    }

    @Override
    public void register(RegisterDTO dto) {
        User user = this.findUserByUserName(dto.getUserName());
        if (!Objects.isNull(user)) {
            throw new GlobalException(ResultCode.USERNAME_ALREADY_REGISTER);
        }
        // 校验用户名
        if (RegexUtil.isPhone(dto.getUserName()) || RegexUtil.isEmail(dto.getUserName())) {
            throw new GlobalException("用户名不合法");
        }
        if(!dto.getUserName().equals(sensitiveFilterUtil.filter(dto.getUserName()))){
            throw new GlobalException("用户名包含敏感字符");
        }
        if(!dto.getNickName().equals(sensitiveFilterUtil.filter(dto.getNickName()))){
            throw new GlobalException("昵称包含敏感字符");
        }
        user = new User();
        // 手机、验证码校验
        if (RegisterMode.PHONE.getCode().equals(dto.getMode())) {
            if (!RegexUtil.isPhone(dto.getPhone())) {
                throw new GlobalException("手机号格式不合法");
            }
            if (isExistPhone(dto.getPhone())) {
                throw new GlobalException("该手机号已被注册");
            }
            if (!captchaService.vertify(CaptchaType.SMS, dto.getPhone(), dto.getCode())) {
                throw new GlobalException("验证码错误");
            }
            user.setPhone(dto.getPhone());
        }
        // 邮箱、验证码校验
        if (RegisterMode.EMAIL.getCode().equals(dto.getMode())) {
            if (!RegexUtil.isEmail(dto.getEmail())) {
                throw new GlobalException("邮箱格式不合法");
            }
            if (isExistEmail(dto.getEmail())) {
                throw new GlobalException("该邮箱已被注册");
            }
            if (!captchaService.vertify(CaptchaType.MAIL, dto.getEmail(), dto.getCode())) {
                throw new GlobalException("验证码错误");
            }
            user.setEmail(dto.getEmail());
        }
        // 保存用户信息
        user.setUserName(dto.getUserName());
        user.setNickName(dto.getNickName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        this.save(user);
        log.info("注册用户，用户id:{},用户名:{},昵称:{}", user.getId(), dto.getUserName(), dto.getNickName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unregister() {
        UserSession session = SessionContext.getSession();
        // 修改用户状态
        User user = this.getById(session.getUserId());
        if (user.getType().equals(UserType.OPEN_ACCOUNT.getValue())) {
            throw new GlobalException("您当前使用的是公开体验账号,不允许注销");
        }
        LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(User::getId,user.getId());
        wrapper.set(User::getCid,Strings.EMPTY);
        wrapper.set(User::getStatus,UserStatus.UN_REG.getValue());
        // 释放手机号和邮箱，否则会无法重新注册
        wrapper.set(User::getPhone,null);
        wrapper.set(User::getEmail,null);
        this.update(wrapper);
        // 清理redis中的cid
        String key1 = StrUtil.join(":", RedisKey.IM_USER_CID, user.getId());
        redisTemplate.delete(key1);
        // 推送消息让用户下线
        SystemMessageVO msgInfo = new SystemMessageVO();
        msgInfo.setType(MessageType.USER_UNREG.code());
        msgInfo.setSendTime(new Date());
        IMSystemMessage<SystemMessageVO> sendMessage = new IMSystemMessage<>();
        sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendSystemMessage(sendMessage);
    }

    @Override
    public void modifyPassword(ModifyPwdDTO dto) {
        UserSession session = SessionContext.getSession();
        User user = this.getById(session.getUserId());
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new GlobalException("旧密码不正确");
        }
        if (user.getType().equals(UserType.OPEN_ACCOUNT.getValue())) {
            throw new GlobalException("您当前使用的是公开体验账号,不允许修改密码");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(user);
        log.info("用户修改密码，用户id:{},用户名:{},昵称:{}", user.getId(), user.getUserName(), user.getNickName());
    }

    @Override
    public User findUserByUserName(String username) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getUserName, username);
        return this.getOne(queryWrapper);
    }

    @Override
    public User findUserByPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getPhone, phone);
        return this.getOne(queryWrapper);
    }

    @Override
    public User findUserByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getEmail, email);
        return this.getOne(queryWrapper);
    }

    @Override
    public User findUserByLoginName(String loginName) {
        // 优先用户名登陆
        User user = findUserByUserName(loginName);
        // 手机号登陆
        if (Objects.isNull(user) && RegexUtil.isPhone(loginName)) {
            user = findUserByPhone(loginName);
        }
        // 邮箱登陆
        if (Objects.isNull(user) && RegexUtil.isEmail(loginName)) {
            user = findUserByEmail(loginName);
        }
        return user;
    }

    @Override
    public Boolean isExistPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getPhone, phone);
        return this.exists(queryWrapper);
    }

    @Override
    public Boolean isExistEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getEmail, email);
        return this.exists(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(UserVO vo) {
        UserSession session = SessionContext.getSession();
        if(!vo.getNickName().equals(sensitiveFilterUtil.filter(vo.getNickName()))){
            throw new GlobalException("昵称包含敏感字符");
        }
        if (!session.getUserId().equals(vo.getId())) {
            throw new GlobalException("不允许修改其他用户的信息!");
        }
        User user = this.getById(vo.getId());
        if (Objects.isNull(user)) {
            throw new GlobalException("用户不存在");
        }
        if (!user.getNickName().equals(vo.getNickName()) || !user.getHeadImageThumb().equals(vo.getHeadImageThumb())) {
            // 更新好友昵称和头像
            LambdaUpdateWrapper<Friend> wrapper1 = Wrappers.lambdaUpdate();
            wrapper1.eq(Friend::getFriendId, session.getUserId());
            wrapper1.set(Friend::getFriendNickName, vo.getNickName());
            wrapper1.set(Friend::getFriendHeadImage, vo.getHeadImageThumb());
            friendService.update(wrapper1);
            // 更新群聊中的昵称和头像
            LambdaUpdateWrapper<GroupMember> wrapper2 = Wrappers.lambdaUpdate();
            wrapper2.eq(GroupMember::getUserId, session.getUserId());
            wrapper2.set(GroupMember::getHeadImage, vo.getHeadImageThumb());
            wrapper2.set(GroupMember::getUserNickName, vo.getNickName());
            groupMemberService.update(wrapper2);
        }
        // 更新用户信息
        user.setNickName(vo.getNickName());
        user.setSex(vo.getSex());
        user.setSignature(vo.getSignature());
        user.setHeadImage(vo.getHeadImage());
        user.setHeadImageThumb(vo.getHeadImageThumb());
        this.updateById(user);
        log.info("用户信息更新，用户:{}}", user);
    }

    @Override
    public UserVO findUserById(Long id) {
        UserSession session = SessionContext.getSession();
        User user = this.getById(id);
        UserVO vo = BeanUtils.copyProperties(user, UserVO.class);
        vo.setOnline(imClient.isOnline(id));
        vo.setIsInBlacklist(userBlacklistService.isInBlacklist(session.getUserId(), id));
        return vo;
    }

    @Override
    public List<UserVO> findUserByName(String name) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(User::getUserName, name).or().like(User::getNickName, name).last("limit 20");
        List<User> users = this.list(queryWrapper);
        return convert(users);
    }

    @Override
    public List<UserVO> search(String name) {
        if (RegexUtil.isPhone(name)) {
            // 查询手机号
            User user = findUserByPhone(name);
            if (!Objects.isNull(user)) {
                return convert(List.of(user));
            }
        } else if (RegexUtil.isEmail(name)) {
            // 查询邮箱
            User user = findUserByEmail(name);
            if (!Objects.isNull(user)) {
                return convert(List.of(user));
            }
        } else {
            // 查询用户名和昵称
            return findUserByName(name);
        }
        return Lists.newArrayList();
    }

    @Override
    public List<OnlineTerminalVO> getOnlineTerminals(String userIds) {
        List<Long> userIdList = Arrays.stream(userIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        // 查询在线的终端
        Map<Long, List<IMTerminalType>> terminalMap = imClient.getOnlineTerminal(userIdList);
        // 组装vo
        List<OnlineTerminalVO> vos = new LinkedList<>();
        terminalMap.forEach((userId, types) -> {
            List<Integer> terminals = types.stream().map(IMTerminalType::code).collect(Collectors.toList());
            vos.add(new OnlineTerminalVO(userId, terminals));
        });
        return vos;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reportCid(String cid) {
        UserSession session = SessionContext.getSession();
        // 清理该设备以前登录过的cid
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getCid, cid);
        wrapper.ne(User::getId, session.getUserId());
        List<User> users = this.list(wrapper);
        users.forEach(user -> {
            // 清理redis中的cid
            String key1 = StrUtil.join(":", RedisKey.IM_USER_CID, user.getId());
            redisTemplate.delete(key1);
            // 清空通知会话信息
            String key2 = StrUtil.join(":", RedisKey.IM_NOTIFY_OFFLINE_SESSION, user.getId());
            redisTemplate.delete(key2);
            user.setCid(Strings.EMPTY);
            this.updateById(user);
        });
        // 保存当前用户的cid
        User user = this.getById(session.getUserId());
        user.setCid(cid);
        this.updateById(user);
        // 缓存cid到redis
        String key = StrUtil.join(":", RedisKey.IM_USER_CID, user.getId());
        redisTemplate.opsForValue().set(key, cid, notifyProps.getActiveDays(), TimeUnit.DAYS);
        // 清空通知会话信息
        String key2 = StrUtil.join(":", RedisKey.IM_NOTIFY_OFFLINE_SESSION, user.getId());
        redisTemplate.delete(key2);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeCid() {
        UserSession session = SessionContext.getSession();
        User user = this.getById(session.getUserId());
        user.setCid(Strings.EMPTY);
        this.updateById(user);
        // 清理redis中的cid
        String key1 = StrUtil.join(":", RedisKey.IM_USER_CID, user.getId());
        redisTemplate.delete(key1);
    }

    @Override
    public void setManualApprove(Boolean enabled) {
        UserSession session = SessionContext.getSession();
        LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(User::getId, session.getUserId());
        wrapper.set(User::getIsManualApprove, enabled);
        this.update(wrapper);
    }

    @Transactional
    @Override
    public void bindPhone(BindPhoneDTO dto) {
        UserSession session = SessionContext.getSession();
        User user = getById(session.getUserId());
        if(StrUtil.isNotEmpty(user.getPhone())){
            throw new GlobalException("您已绑定了手机号,不可重复绑定");
        }
        if (!RegexUtil.isPhone(dto.getPhone())) {
            throw new GlobalException("手机号格式不合法");
        }
        if (isExistPhone(dto.getPhone())) {
            throw new GlobalException("该手机号已被注册");
        }
        if (!captchaService.vertify(CaptchaType.SMS, dto.getPhone(), dto.getCode())) {
            throw new GlobalException("验证码错误");
        }
        user.setPhone(dto.getPhone());
        this.updateById(user);
    }

    @Transactional
    @Override
    public void bindEmail(BindEmailDTO dto) {
        UserSession session = SessionContext.getSession();
        User user = getById(session.getUserId());
        if(StrUtil.isNotEmpty(user.getEmail())){
            throw new GlobalException("您已绑定了邮箱,不可重复绑定");
        }
        if (!RegexUtil.isEmail(dto.getEmail())) {
            throw new GlobalException("邮箱格式不合法");
        }
        if (isExistEmail(dto.getEmail())) {
            throw new GlobalException("该邮箱已被注册");
        }
        if (!captchaService.vertify(CaptchaType.MAIL, dto.getEmail(), dto.getCode())) {
            throw new GlobalException("验证码错误");
        }
        user.setEmail(dto.getEmail());
        this.updateById(user);
    }

    List<UserVO> convert(List<User> users) {
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        List<Long> onlineUserIds = imClient.getOnlineUser(userIds);
        return users.stream().map(u -> {
            UserVO vo = BeanUtils.copyProperties(u, UserVO.class);
            vo.setOnline(onlineUserIds.contains(u.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

}
