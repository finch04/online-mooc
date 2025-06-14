package com.tianji.message.controller;

import com.alibaba.fastjson.JSONObject;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.message.domain.dto.ChatMessageDTO;
import com.tianji.message.domain.dto.PrivateMessageDTO;
import com.tianji.message.domain.dto.UserSimpleDTO;
import com.tianji.message.domain.po.ChatMessage;
import com.tianji.message.service.IChatMessageService;
import com.tianji.message.utils.SensitiveWordDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpAttributes;
import org.springframework.messaging.simp.SimpAttributesContextHolder;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final UserClient userClient; // 用户微服务客户端
    private final IChatMessageService messageService;

    private static final String ONLINE_USERS_KEY = "online_users";
    private static final String GROUP_USERS_KEY_PREFIX = "group_users:";

    // 发送在线用户信息到前端（包含用户详情）
    private void sendOnlineUserInfo() {
        Set<String> onlineUserIds = redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
        Long count = redisTemplate.opsForSet().size(ONLINE_USERS_KEY);

        // 调用用户微服务批量获取用户信息
        List<UserDTO> userInfoList = userClient.queryUserByIds(
                onlineUserIds.stream()
                        .map(Long::valueOf) // 将String转为Long
                        .collect(Collectors.toList())
        );

        // 将 UserDTO 转换为 UserSimpleDTO
        List<UserSimpleDTO> simpleUserInfoList = userInfoList.stream()
                .map(user -> BeanUtils.copyProperties(user, UserSimpleDTO.class))
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        data.put("userInfoList", simpleUserInfoList); // 包含id、name、icon的用户详情列表
        messagingTemplate.convertAndSend("/topic/onlineUsers", data);
    }

    // 私聊消息处理（添加用户详情解析）
    @MessageMapping("/privateMessage")
    public void handlePrivateMessage(@Payload PrivateMessageDTO message) {
        SimpAttributes simpAttributes = SimpAttributesContextHolder.currentAttributes();
        Long senderId = (Long) simpAttributes.getAttribute("userId");

        boolean b = SensitiveWordDetector.containsSensitiveWord(message.getContent());
        if (b) {
            throw new BadRequestException("聊天消息有违禁词！");

        }

        // 构建响应消息（包含发送者姓名和头像，需从用户微服务获取）
        UserContext.setUser(senderId);
        UserDTO sender = userClient.queryUserById(senderId);
        UserContext.removeUser();

        UserSimpleDTO userSimpleDTO = BeanUtils.copyProperties(sender, UserSimpleDTO.class);

        // 发送给接收者（携带用户详情，通过消息头传递）
        Map<String, Object> headers = new HashMap<>();
        headers.put("senderId", userSimpleDTO.getId());
        headers.put("senderName", userSimpleDTO.getName());
        headers.put("senderIcon", userSimpleDTO.getIcon());

        messagingTemplate.convertAndSend(
                "/queue/private/" + message.getRecipientId(),
                message.getContent(),
                new MessageHeaders(headers)
        );

        // 发送回执给发送者（同理添加用户详情）
        messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                "/queue/private",
                "消息已发送至: " + message.getRecipientId(),
                createHeaders(userSimpleDTO)
        );

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSenderId(senderId);
        dto.setContent(message.getContent());
        dto.setTargetId(Long.valueOf(message.getRecipientId()));
        dto.setMessageType(1);  //私聊
        messageService.sendMessage(dto);
    }

    // 广播消息处理（添加用户详情）
    @MessageMapping("/broadcast")
    @SendTo("/topic/messages")
    public String handleBroadcast(@Payload String message) {
        SimpAttributes simpAttributes = SimpAttributesContextHolder.currentAttributes();
        Long senderId = (Long) simpAttributes.getAttribute("userId");

        boolean b = SensitiveWordDetector.containsSensitiveWord(message);
        if (b) {
            throw new BadRequestException("聊天消息有违禁词！");
        }

        // 构建响应消息（包含发送者姓名和头像，需从用户微服务获取）
        UserContext.setUser(senderId);
        UserDTO sender = userClient.queryUserById(senderId);
        UserContext.removeUser();

        UserSimpleDTO userSimpleDTO = BeanUtils.copyProperties(sender, UserSimpleDTO.class);

        // 发送给接收者（携带用户详情，通过消息头传递）
        Map<String, Object> headers = new HashMap<>();
        headers.put("senderId", userSimpleDTO.getId());
        headers.put("senderName", userSimpleDTO.getName());
        headers.put("senderIcon", userSimpleDTO.getIcon());

        messagingTemplate.convertAndSend(
                "/topic/messages",
                String.format(" %s",
                        message
                ),
                new MessageHeaders(headers)
        );
        log.info("广播消息已发送：{}", userSimpleDTO);

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSenderId(senderId);
        dto.setContent(message);
        dto.setTargetId(0L); //广播消息默认发给id为0，并且是群聊类型
        dto.setMessageType(2);  //群聊
        messageService.sendMessage(dto);

        return null;
    }

    // 辅助方法：创建包含用户详情的消息头
    private MessageHeaders createHeaders(UserSimpleDTO dto) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("senderId", dto.getId());
        headers.put("senderName", dto.getName());
        headers.put("senderIcon", dto.getIcon());
        return new MessageHeaders(headers);
    }

    // 用户连接时添加到在线用户列表
    @MessageMapping("/connect")
    public void handleConnect(@Header("user-id") String userId) {
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);
        sendOnlineUserInfo();
    }

    // 用户断开连接时从在线用户列表移除
    @MessageMapping("/disconnect")
    public void handleDisconnect(@Header("user-id") String userId) {
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);
        sendOnlineUserInfo();
    }

    // 查询在线用户列表
    @GetMapping("/online-users")
    public Set<String> getOnlineUsers() {
        return redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
    }

    // 查询在线人数
    @GetMapping("/online-count")
    public Long getOnlineCount() {
        return redisTemplate.opsForSet().size(ONLINE_USERS_KEY);
    }

    // 用户加入群组
    @MessageMapping("/joinGroup")
    public void joinGroup( @Header("group-id") String groupId) {
        SimpAttributes simpAttributes = SimpAttributesContextHolder.currentAttributes();
        Long userId = (Long) simpAttributes.getAttribute("userId");
        String groupUsersKey = GROUP_USERS_KEY_PREFIX + groupId;
        redisTemplate.opsForSet().add(groupUsersKey, userId.toString());
    }

    // 用户退出群组
    @MessageMapping("/leaveGroup")
    public void leaveGroup( @Header("group-id") String groupId) {
        SimpAttributes simpAttributes = SimpAttributesContextHolder.currentAttributes();
        Long userId = (Long) simpAttributes.getAttribute("userId");
        String groupUsersKey = GROUP_USERS_KEY_PREFIX + groupId;
        redisTemplate.opsForSet().remove(groupUsersKey, userId);
    }

    // 群组消息处理
    @MessageMapping("/group/{groupId}")
    public void handleGroupMessage(@Header("group-id") String groupId, @Header("group-name") String groupName, @Payload String message) {
        SimpAttributes simpAttributes = SimpAttributesContextHolder.currentAttributes();
        Long senderId = (Long) simpAttributes.getAttribute("userId");

        boolean b = SensitiveWordDetector.containsSensitiveWord(message);
        if (b) {
            throw new BadRequestException("聊天消息有违禁词！");
        }

        // 构建响应消息（包含发送者姓名和头像，需从用户微服务获取）
        UserContext.setUser(senderId);
        UserDTO sender = userClient.queryUserById(senderId);
        UserContext.removeUser();

        UserSimpleDTO userSimpleDTO = BeanUtils.copyProperties(sender, UserSimpleDTO.class);

        // 发送给接收者（携带用户详情和群组信息，通过消息头传递）
        Map<String, Object> headers = new HashMap<>();
        headers.put("senderId", userSimpleDTO.getId());
        headers.put("senderName", userSimpleDTO.getName());
        headers.put("senderIcon", userSimpleDTO.getIcon());
        headers.put("groupId", groupId);
        headers.put("groupName", groupName);

        messagingTemplate.convertAndSend(
                "/topic/group/" + groupId,
                message,
                new MessageHeaders(headers)
        );

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSenderId(senderId);
        dto.setContent(message);
        dto.setTargetId(Long.valueOf(groupId));
        dto.setMessageType(2);  //群聊
        messageService.sendMessage(dto);

        log.info("群组消息已发送到群组 {}，消息内容: {}", groupId, message);
    }


}