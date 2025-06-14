package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.dto.FriendRemarkDTO;
import com.bx.implatform.entity.Friend;
import com.bx.implatform.vo.FriendVO;

import java.util.List;
import java.util.Map;

public interface FriendService extends IService<Friend> {

    /**
     * 判断用户2是否用户1的好友
     *
     * @param userId1 用户1的id
     * @param userId2 用户2的id
     * @return true/false
     */
    Boolean isFriend(Long userId1, Long userId2);


    /**
     * 查询用户的所有好友,包括已删除的
     *
     * @return 好友列表
     */
    List<Friend> findAllFriends();

    /**
     * 查询用户的所有好友
     *
     * @param friendIds 好友id
     * @return 好友列表
     */
    List<Friend> findByFriendIds(List<Long> friendIds);

    /**
     * 查询当前用户的所有好友
     *
     * @return 好友列表
     */
    List<FriendVO> findFriends();

    /**
     * 添加好友，互相建立好友关系
     *
     * @param friendId 好友的用户id
     */
    void addFriend(Long friendId);

    /**
     * 删除好友，双方都会解除好友关系
     *
     * @param friendId 好友的用户id
     */
    void delFriend(Long friendId);

    /**
     * 更新好友信息，主要是头像和昵称
     *
     * @param vo 好友vo
     */
    void update(FriendVO vo);

    /**
     * 查询指定的某个好友信息
     *
     * @param friendId 好友的用户id
     * @return 好友信息
     */
    FriendVO findFriend(Long friendId);

    /**
     * 绑定好友关系
     *
     * @param userId 好友的id
     * @param friendId 好友的用户id
     * @return 好友信息
     */
    void bindFriend(Long userId, Long friendId);

    /**
     * 修改好友备注
     *
     * @param dto dto
     * @return 好友信息
     */
    FriendVO modifyRemark(FriendRemarkDTO dto);

    /**
     * 查询用户昵称
     *
     * @param friendIds 用户id列表
     * @return 昵称
     */
    Map<Long,String> loadRemark(List<Long> friendIds);

    /**
     * 推送在线状态给所有好友
     * @param userId 用户id
     * @param terminal 终端类型
     */
    void sendOnlineStatus(Long userId,Integer terminal);
}
