package com.bx.implatform.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.IMConstant;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.imcommon.model.IMGroupMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.imcommon.util.CommaTextUtils;
import com.bx.imcommon.util.ThreadPoolExecutorFactory;
import com.bx.implatform.annotation.OnlineCheck;
import com.bx.implatform.contant.Constant;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.dto.GroupMessageDTO;
import com.bx.implatform.entity.Group;
import com.bx.implatform.entity.GroupMember;
import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.GroupMessageMapper;
import com.bx.implatform.service.GroupMemberService;
import com.bx.implatform.service.GroupMessageService;
import com.bx.implatform.service.GroupService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.util.SensitiveFilterUtil;
import com.bx.implatform.vo.GroupMessageVO;
import com.bx.implatform.vo.QuoteMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupMessageServiceImpl extends ServiceImpl<GroupMessageMapper, GroupMessage>
    implements GroupMessageService {
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IMClient imClient;
    private final SensitiveFilterUtil sensitiveFilterUtil;
    private static final ScheduledThreadPoolExecutor EXECUTOR = ThreadPoolExecutorFactory.getThreadPoolExecutor();

    @Override
    public GroupMessageVO sendMessage(GroupMessageDTO dto) {
        UserSession session = SessionContext.getSession();
        Group group = groupService.getAndCheckById(dto.getGroupId());
        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getGroupId(), session.getUserId());
        if (group.getIsMuted() && !group.getOwnerId().equals(session.getUserId()) && !member.getIsManager()) {
            throw new GlobalException("群主开启了全员禁言模式,无法发送消息");
        }
        // 是否在群聊里面
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException("您已不在群聊里面，无法发送消息");
        }
        if (member.getIsMuted()) {
            throw new GlobalException("您已被禁言，无法发送消息");
        }
        // 群聊成员列表
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(group.getId());
        if (dto.getReceipt() && userIds.size() > Constant.MAX_NORMAL_GROUP_MEMBER) {
            // 大群的回执消息过于消耗资源，不允许发送
            throw new GlobalException(
                String.format("当前群聊大于%s人,不支持发送回执消息", Constant.MAX_NORMAL_GROUP_MEMBER));
        }
        // 不用发给自己
        userIds = userIds.stream().filter(id -> !session.getUserId().equals(id)).collect(Collectors.toList());
        // 保存消息
        GroupMessage msg = BeanUtils.copyProperties(dto, GroupMessage.class);
        msg.setSendId(session.getUserId());
        msg.setSendTime(new Date());
        msg.setSendNickName(member.getShowNickName());
        msg.setAtUserIds(CommaTextUtils.asText(dto.getAtUserIds()));
        // 过滤内容中的敏感词
        if (MessageType.TEXT.code().equals(dto.getType())) {
            msg.setContent(sensitiveFilterUtil.filter(dto.getContent()));
        }
        this.save(msg);

        // 群发
        GroupMessageVO msgInfo = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        // 填充引用消息
        if (!Objects.isNull(dto.getQuoteMessageId())) {
            GroupMessage quoteMessage = this.getById(dto.getQuoteMessageId());
            msgInfo.setQuoteMessage(BeanUtils.copyProperties(quoteMessage, QuoteMessageVO.class));
        }
        msgInfo.setAtUserIds(dto.getAtUserIds());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);
        log.info("发送群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), dto.getGroupId(), dto.getContent());
        return msgInfo;
    }

    @Transactional
    @Override
    public GroupMessageVO recallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        GroupMessage msg = this.getById(id);
        if (Objects.isNull(msg)) {
            throw new GlobalException("消息不存在");
        }
        if (!msg.getSendId().equals(session.getUserId())) {
            throw new GlobalException("这条消息不是由您发送,无法撤回");
        }
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > IMConstant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException("消息已发送超过5分钟，无法撤回");
        }
        // 判断是否在群里
        GroupMember member = groupMemberService.findByGroupAndUserId(msg.getGroupId(), session.getUserId());
        if (Objects.isNull(member) || Boolean.TRUE.equals(member.getQuit())) {
            throw new GlobalException("您已不在群聊里面，无法撤回消息");
        }
        // 修改数据库
        msg.setStatus(MessageStatus.RECALL.code());
        this.updateById(msg);
        // 生成一条撤回消息
        GroupMessage recallMsg = new GroupMessage();
        recallMsg.setStatus(MessageStatus.UNSEND.code());
        recallMsg.setType(MessageType.RECALL.code());
        recallMsg.setGroupId(msg.getGroupId());
        recallMsg.setSendId(session.getUserId());
        recallMsg.setSendNickName(member.getShowNickName());
        recallMsg.setContent(id.toString());
        recallMsg.setSendTime(new Date());
        this.save(recallMsg);
        // 群发
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(msg.getGroupId());
        GroupMessageVO msgInfo = BeanUtils.copyProperties(recallMsg, GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);
        log.info("撤回群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), msg.getGroupId(), msg.getContent());
        return msgInfo;
    }

    @OnlineCheck
    @Override
    public void pullOfflineMessage(Long minId) {
        UserSession session = SessionContext.getSession();
        // 查询用户加入的群组
        List<GroupMember> members = groupMemberService.findByUserId(session.getUserId());
        Map<Long, GroupMember> groupMemberMap = CollStreamUtil.toIdentityMap(members, GroupMember::getGroupId);
        Set<Long> groupIds = groupMemberMap.keySet();
        if (CollectionUtil.isEmpty(groupIds)) {
            // 关闭加载中标志
            this.sendLoadingMessage(false, session);
            return;
        }
        // 只能拉取最近3个月的,移动端只拉一个月
        int months = session.getTerminal().equals(IMTerminalType.APP.code()) ? 1 : 3;
        Date minDate = DateUtils.addMonths(new Date(), -months);
        LambdaQueryWrapper<GroupMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.gt(GroupMessage::getId, minId).gt(GroupMessage::getSendTime, minDate)
            .in(GroupMessage::getGroupId, groupIds).ne(GroupMessage::getStatus, MessageStatus.RECALL.code())
            .orderByAsc(GroupMessage::getId);
        List<GroupMessage> messages = this.list(wrapper);
        // 通过群聊对消息进行分组
        Map<Long, List<GroupMessage>> messageGroupMap =
            messages.stream().collect(Collectors.groupingBy(GroupMessage::getGroupId));
        // 退群前的消息
        List<GroupMember> quitMembers = groupMemberService.findQuitInMonth(session.getUserId());
        for (GroupMember quitMember : quitMembers) {
            wrapper = Wrappers.lambdaQuery();
            wrapper.gt(GroupMessage::getId, minId).between(GroupMessage::getSendTime, minDate, quitMember.getQuitTime())
                .eq(GroupMessage::getGroupId, quitMember.getGroupId()).orderByAsc(GroupMessage::getId);
            List<GroupMessage> groupMessages = this.list(wrapper);
            messageGroupMap.put(quitMember.getGroupId(), groupMessages);
            groupMemberMap.put(quitMember.getGroupId(), quitMember);
        }
        // 异步推送消息
        EXECUTOR.execute(() -> {
            Long time = System.currentTimeMillis();
            // 开启加载中标志
            this.sendLoadingMessage(true, session);
            AtomicInteger sendCount = new AtomicInteger();
            messageGroupMap.forEach((groupId, groupMessages) -> {
                // 引用消息
                Map<Long, QuoteMessageVO> quoteMessageMap = batchLoadQuoteMessage(groupMessages);
                // 填充消息状态
                String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
                Object o = redisTemplate.opsForHash().get(key, session.getUserId().toString());
                long readedMaxId = Objects.isNull(o) ? -1 : Long.parseLong(o.toString());
                Map<Object, Object> maxIdMap = null;
                // 第一次拉取时,一个群最多推送1w条消息，防止前端接收能力溢出导致卡顿
                List<GroupMessage> sendMessages = groupMessages;
                if (minId <= 0 && groupMessages.size() > 10000) {
                    sendMessages = groupMessages.subList(groupMessages.size() - 10000, groupMessages.size());
                }
                for (GroupMessage m : sendMessages) {
                    // 排除加群之前的消息
                    GroupMember member = groupMemberMap.get(m.getGroupId());
                    if (DateUtil.compare(member.getCreatedTime(), m.getSendTime()) > 0) {
                        continue;
                    }
                    // 排除不需要接收的消息
                    List<String> recvIds = CommaTextUtils.asList(m.getRecvIds());
                    if (!recvIds.isEmpty() && !recvIds.contains(session.getUserId().toString())) {
                        continue;
                    }
                    // 组装vo
                    GroupMessageVO vo = BeanUtils.copyProperties(m, GroupMessageVO.class);
                    // 引用消息
                    vo.setQuoteMessage(quoteMessageMap.get(m.getQuoteMessageId()));
                    // 被@用户列表
                    List<String> atIds = CommaTextUtils.asList(m.getAtUserIds());
                    vo.setAtUserIds(atIds.stream().map(Long::parseLong).collect(Collectors.toList()));
                    // 填充状态
                    vo.setStatus(readedMaxId >= m.getId() ? MessageStatus.READED.code() : MessageStatus.UNSEND.code());
                    // 针对回执消息填充已读人数
                    if (m.getReceipt()) {
                        if (Objects.isNull(maxIdMap)) {
                            maxIdMap = redisTemplate.opsForHash().entries(key);
                        }
                        int count = getReadedUserIds(maxIdMap, m.getId(), m.getSendId()).size();
                        vo.setReadedCount(count);
                    }
                    // 推送
                    IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
                    sendMessage.setSender(new IMUserInfo(m.getSendId(), IMTerminalType.WEB.code()));
                    sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
                    sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
                    sendMessage.setSendResult(false);
                    sendMessage.setSendToSelf(false);
                    sendMessage.setData(vo);
                    imClient.sendGroupMessage(sendMessage);
                    sendCount.getAndIncrement();
                }
            });
            log.info("耗时：{}", System.currentTimeMillis() - time);
            // 关闭加载中标志
            this.sendLoadingMessage(false, session);
            log.info("拉取离线群聊消息,用户id:{},数量:{},耗时:{}", session.getUserId(), sendCount.get(),
                System.currentTimeMillis() - time);
        });
    }

    @Override
    public void readedMessage(Long groupId) {
        UserSession session = SessionContext.getSession();
        // 取出最后的消息id
        LambdaQueryWrapper<GroupMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(GroupMessage::getGroupId, groupId).orderByDesc(GroupMessage::getId).last("limit 1")
            .select(GroupMessage::getId);
        GroupMessage message = this.getOne(wrapper);
        if (Objects.isNull(message)) {
            return;
        }
        // 推送消息给自己的其他终端,同步清空会话列表中的未读数量
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.READED.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setSendId(session.getUserId());
        msgInfo.setGroupId(groupId);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setSendToSelf(true);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendGroupMessage(sendMessage);
        // 已读消息key
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        // 原来的已读消息位置
        Object maxReadedId = redisTemplate.opsForHash().get(key, session.getUserId().toString());
        // 记录已读消息位置
        redisTemplate.opsForHash().put(key, session.getUserId().toString(), message.getId());
        // 推送消息回执，刷新已读人数显示
        wrapper = Wrappers.lambdaQuery();
        wrapper.eq(GroupMessage::getGroupId, groupId);
        wrapper.gt(!Objects.isNull(maxReadedId), GroupMessage::getId, maxReadedId);
        wrapper.le(!Objects.isNull(maxReadedId), GroupMessage::getId, message.getId());
        wrapper.ne(GroupMessage::getStatus, MessageStatus.RECALL.code());
        wrapper.eq(GroupMessage::getReceipt, true);
        List<GroupMessage> receiptMessages = this.list(wrapper);
        if (CollectionUtil.isNotEmpty(receiptMessages)) {
            List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
            Map<Object, Object> maxIdMap = redisTemplate.opsForHash().entries(key);
            for (GroupMessage receiptMessage : receiptMessages) {
                int readedCount = getReadedUserIds(maxIdMap, receiptMessage.getId(), receiptMessage.getSendId()).size();
                // 如果所有人都已读，记录回执消息完成标记
                if (readedCount >= userIds.size() - 1) {
                    receiptMessage.setReceiptOk(true);
                    this.updateById(receiptMessage);
                }
                msgInfo = new GroupMessageVO();
                msgInfo.setId(receiptMessage.getId());
                msgInfo.setGroupId(groupId);
                msgInfo.setReadedCount(readedCount);
                msgInfo.setReceiptOk(receiptMessage.getReceiptOk());
                msgInfo.setType(MessageType.RECEIPT.code());
                sendMessage = new IMGroupMessage<>();
                sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
                sendMessage.setRecvIds(userIds);
                sendMessage.setData(msgInfo);
                sendMessage.setSendToSelf(false);
                sendMessage.setSendResult(false);
                imClient.sendGroupMessage(sendMessage);
            }
        }
    }

    @Override
    public List<Long> findReadedUsers(Long groupId, Long messageId) {
        UserSession session = SessionContext.getSession();
        GroupMessage message = this.getById(messageId);
        if (Objects.isNull(message)) {
            throw new GlobalException("消息不存在");
        }
        // 是否在群聊里面
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, session.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException("您已不在群聊里面");
        }
        // 已读位置key
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        // 一次获取所有用户的已读位置
        Map<Object, Object> maxIdMap = redisTemplate.opsForHash().entries(key);
        // 返回已读用户的id集合
        return getReadedUserIds(maxIdMap, message.getId(), message.getSendId());
    }

    @Override
    public List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        Long userId = SessionContext.getSession().getUserId();
        long stIdx = (page - 1) * size;
        // 群聊成员信息
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, userId);
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException("您已不在群聊中");
        }
        // 查询聊天记录，只查询加入群聊时间之后的消息
        QueryWrapper<GroupMessage> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMessage::getGroupId, groupId).gt(GroupMessage::getSendTime, member.getCreatedTime())
            .ne(GroupMessage::getStatus, MessageStatus.RECALL.code()).orderByDesc(GroupMessage::getId)
            .last("limit " + stIdx + "," + size);
        List<GroupMessage> messages = this.list(wrapper);
        List<GroupMessageVO> messageInfos =
            messages.stream().map(m -> BeanUtils.copyProperties(m, GroupMessageVO.class)).collect(Collectors.toList());
        log.info("拉取群聊记录，用户id:{},群聊id:{}，数量:{}", userId, groupId, messageInfos.size());
        return messageInfos;
    }

    private List<Long> getReadedUserIds(Map<Object, Object> maxIdMap, Long messageId, Long sendId) {
        List<Long> userIds = new LinkedList<>();
        maxIdMap.forEach((k, v) -> {
            Long userId = Long.valueOf(k.toString());
            long maxId = Long.parseLong(v.toString());
            // 发送者不计入已读人数
            if (!sendId.equals(userId) && maxId >= messageId) {
                userIds.add(userId);
            }
        });
        return userIds;
    }

    private void sendLoadingMessage(Boolean isLoading, UserSession session) {
        ;
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.LOADING.code());
        msgInfo.setContent(isLoading.toString());
        IMGroupMessage sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
        sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        imClient.sendGroupMessage(sendMessage);
    }

    private Map<Long, QuoteMessageVO> batchLoadQuoteMessage(List<GroupMessage> messages) {
        // 提取列表中所有引用消息
        List<Long> ids =
            messages.stream().filter(m -> !Objects.isNull(m.getQuoteMessageId())).map(GroupMessage::getQuoteMessageId)
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(ids)) {
            return new HashMap<>();
        }
        LambdaQueryWrapper<GroupMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.in(GroupMessage::getId, ids);
        List<GroupMessage> quoteMessages = this.list(wrapper);
        // 转为vo
        return quoteMessages.stream()
            .collect(Collectors.toMap(m -> m.getId(), m -> BeanUtils.copyProperties(m, QuoteMessageVO.class)));
    }

}
