package com.tianji.message.service.impl;/**
 * @author fsq
 * @date 2025/5/21 19:02
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.UserContext;
import com.tianji.message.domain.po.UserConversation;
import com.tianji.message.domain.po.UserPrivateMessage;
import com.tianji.message.domain.query.UserConversationQuery;
import com.tianji.message.domain.vo.UserConversationVO;
import com.tianji.message.mapper.UserConversationMapper;
import com.tianji.message.service.IUserConversationService;
import com.tianji.message.service.IUserPrivateMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: fsq
 * @Date: 2025/5/21 19:02
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class UserConversationServiceImpl  extends ServiceImpl<UserConversationMapper, UserConversation> implements IUserConversationService {

    private final UserConversationMapper userConversationMapper;
    private final UserClient userClient;
    private final IUserPrivateMessageService userPrivateMessageService;

    @Override
    public UserConversation getConversationByUserId(Long user1Id, Long user2Id) {
        //根据userId和对方userId找到会话
        UserConversation conversation = userConversationMapper.selectOne(
                new LambdaQueryWrapper<UserConversation>()
                        .eq(UserConversation::getUserId1, user1Id)
                        .eq(UserConversation::getUserId2, user2Id)
                        .or()
                        .eq(UserConversation::getUserId1, user2Id)
                        .eq(UserConversation::getUserId2, user1Id)
        );
        return conversation;
    }

    @Override
    public PageDTO<UserConversationVO> getConversationList(UserConversationQuery query) {
        Long userId = UserContext.getUser();

        // 根据query查询用户会话，分页
        Page<UserConversation> page = new Page<>(query.getPageNo(), query.getPageSize());
        IPage<UserConversation> conversationPage = userConversationMapper.selectConversationList(page, userId, query);

        // 提取对方用户ID列表
        List<Long> otherUserIds = extractOtherUserIds(
                conversationPage.getRecords(),
                userId
        );

        // 批量调用用户服务获取用户信息，添加异常处理
        List<UserDTO> userInfoList = Collections.emptyList();
        try {
            userInfoList = userClient.queryUserByIds(otherUserIds);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            // 可根据业务需求选择返回空列表或抛出特定异常
        }

        // 将用户信息转为Map，优化查找效率
        Map<Long, UserDTO> userInfoMap = userInfoList.stream()
                .collect(Collectors.toMap(UserDTO::getId, user -> user, (u1, u2) -> u1));

        // 组装VO对象
        List<UserConversationVO> voList = conversationPage.getRecords().stream()
                .map(conversation -> {
                    boolean isA = conversation.getUserId1().equals(userId);
                    Long otherUserId = isA ? conversation.getUserId2() : conversation.getUserId1();

                    // 从Map中获取用户信息，避免空指针
                    UserDTO userInfo = userInfoMap.getOrDefault(otherUserId, null);

                    UserConversationVO vo = new UserConversationVO();
                    vo.setId(conversation.getId());
                    vo.setOtherUserId(otherUserId);

                    // 设置对方用户名和头像，处理可能的空值
                    if (userInfo != null) {
                        vo.setOtherUsername(userInfo.getName());
                        vo.setOtherAvatar(userInfo.getIcon());
                    } else {
                        vo.setOtherUsername("用户已注销");
                        vo.setOtherAvatar("default_avatar_url");
                    }

                    // 需要实现获取最后一条消息内容的逻辑
                    UserPrivateMessage lastMessage = userPrivateMessageService.getLastMessage(otherUserId);
                    if(lastMessage!=null){
                        vo.setLastMessage(lastMessage.getContent());
                        vo.setLastMessageTime(lastMessage.getPushTime());
                    }
                    vo.setUnReadCount(userPrivateMessageService.getUnreadCount(otherUserId));
                    vo.setStatus(conversation.getStatus());

                    return vo;
                })
                .collect(Collectors.toList());

        // 构建分页结果并返回
        return PageDTO.of(page, voList);
    }


    /**
     * 从会话列表中提取对方用户ID列表
     * @param conversationList 会话列表
     * @param currentUserId 当前用户ID
     * @return 对方用户ID列表
     */
    public  List<Long> extractOtherUserIds(List<UserConversation> conversationList, Long currentUserId) {
        List<Long> otherUserIds = new ArrayList<>();

        for (UserConversation conversation : conversationList) {
            if (conversation.getUserId1().equals(currentUserId)) {
                otherUserIds.add(conversation.getUserId2());
            } else {
                otherUserIds.add(conversation.getUserId1());
            }
        }
        return otherUserIds.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public void  deleteConversation(Long conversationId) {
        // 业务逻辑：直接物理删除（根据需求） 反正删除会话并不删除聊天记录
         userConversationMapper.deleteById(conversationId);
    }

    @Override
    public void blockConversation(Long conversationId) {
        // 业务逻辑：将会话状态设置为"已屏蔽"（status=1）
        userConversationMapper.update(null,
                new UpdateWrapper<UserConversation>()
                        .eq("id",conversationId)
                        .set("status",1)
        );
    }

    @Override
    public void unBlockConversation(Long conversationId) {
        // 业务逻辑：将会话状态设置为"正常"（status=0）
        userConversationMapper.update(null,
                new UpdateWrapper<UserConversation>()
                        .eq("id",conversationId)
                        .set("status",0)
        );
    }

    @Override
    public void createConversation(Long user1Id,Long user2Id) {
        UserConversation conversation = new UserConversation();
        conversation.setUserId1(user1Id);
        conversation.setUserId2(user2Id);
        userConversationMapper.insert(conversation);
    }
}
