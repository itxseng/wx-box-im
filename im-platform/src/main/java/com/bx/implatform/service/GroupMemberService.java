package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.GroupMember;

import java.util.List;

public interface GroupMemberService extends IService<GroupMember> {

    /**
     * 根据群聊id和用户id查询群聊成员
     *
     * @param groupId 群聊id
     * @param userId  用户id
     * @return 群聊成员信息
     */
    GroupMember findByGroupAndUserId(Long groupId, Long userId);

    /**
     * 根据群聊id和用户id查询群聊成员
     *
     * @param groupId 群聊id
     * @param userIds  用户id
     * @return 群聊成员信息
     */
    List<GroupMember> findByGroupAndUserIds(Long groupId, List<Long> userIds);
    /**
     * 根据用户id查询群聊成员
     *
     * @param userId 用户id
     * @return 成员列表
     */
    List<GroupMember> findByUserId(Long userId);

    /**
     * 根据用户id查询一个月内退的群
     *
     * @param userId 用户id
     * @return 成员列表
     */
    List<GroupMember> findQuitInMonth(Long userId);

    /**
     * 根据群聊id查询群聊成员（包括已退出）
     *
     * @param groupId 群聊id
     * @return 群聊成员列表
     */
    List<GroupMember> findByGroupId(Long groupId);


    /**
     * 根据群聊id查询没有退出的群聊成员id
     *
     * @param groupId 群聊id
     * @return 群聊成员id列表
     */
    List<Long> findUserIdsByGroupId(Long groupId);

    /**
     * 批量添加成员
     *
     * @param groupId 群聊id
     * @param members 成员列表
     * @return 成功或失败
     */
    boolean saveOrUpdateBatch(Long groupId, List<GroupMember> members);

    /**
     * 根据群聊id删除移除成员
     *
     * @param groupId 群聊id
     */
    void removeByGroupId(Long groupId);

    /**
     * 根据群聊id和用户id移除成员
     *
     * @param groupId 群聊id
     * @param userId  用户id
     */
    void removeByGroupAndUserId(Long groupId, Long userId);

    /**
     * 根据群聊id和用户id移除成员
     *
     * @param groupId 群聊id
     * @param userIds  用户id
     */
    void removeByGroupAndUserIds(Long groupId, List<Long> userIds);

    /**
     * 用户是否在群中
     *
     * @param groupId 群聊id
     * @param userIds  用户id
     */
    Boolean isInGroup(Long groupId,List<Long> userIds);

    /**
     * 设置禁言
     * @param groupId 群id
     * @param userIds 用户列表
     * @param isMuted 开启/关闭
     */
    void setMuted(Long groupId, List<Long> userIds,Boolean isMuted);

    /**
     * 获取已经禁言/未禁言的用户id列表
     * @param groupId 群id
     * @param userIds 用户列表
     * @param isMuted 开启/关闭
     */
    List<Long> findMutedUserIds(Long groupId,List<Long> userIds, Boolean isMuted);

    /**
     * 更新置顶消息状态标志
     * @param groupId 群id
     * @param isTopMessage 是否显示置顶消息
     */
    void updateTopMessage(Long groupId,Boolean isTopMessage);


    /**
     * 更新置顶消息状态标志
     * @param groupId 群id
     * @param userId 用户id
     * @param isTopMessage 是否显示置顶消息
     */
    void updateTopMessage(Long groupId, Long userId,Boolean isTopMessage);

    /**
     * 设置管理员状态
     * @param groupId 群id
     * @param userIds 用户列表
     * @param isManager 是否管理员
     */
    void  setManager(Long groupId, List<Long> userIds, Boolean isManager);
}
