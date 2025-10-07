package com.tianji.live.service;

import java.util.Set;

/**
 * 用户关注服务接口
 */
public interface IUserFollowService {

    /**
     * 关注用户
     * @param userId 关注者ID
     * @param followedUserId 被关注者ID
     * @return 是否关注成功（true=成功，false=已关注过）
     */
    Boolean follow(Long userId, Long followedUserId);

    /**
     * 取消关注
     * @param userId 关注者ID
     * @param followedUserId 被关注者ID
     * @return 是否取消成功（true=成功，false=未关注）
     */
    Boolean unfollow(Long userId, Long followedUserId);

    /**
     * 判断是否已关注
     * @param userId 关注者ID
     * @param followedUserId 被关注者ID
     * @return true=已关注，false=未关注
     */
    Boolean isFollowing(Long userId, Long followedUserId);

    /**
     * 获取用户关注列表
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 关注的用户ID集合
     */
    Set<String> getFollowList(Long userId, long limit);

    /**
     * 获取用户粉丝列表
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 粉丝用户ID集合
     */
    Set<String> getFansList(Long userId, long limit);

    /**
     * 获取用户关注数量
     * @param userId 用户ID
     * @return 关注数量
     */
    Long getFollowCount(Long userId);

    /**
     * 获取用户粉丝数量
     * @param userId 用户ID
     * @return 粉丝数量
     */
    Long getFansCount(Long userId);
}
