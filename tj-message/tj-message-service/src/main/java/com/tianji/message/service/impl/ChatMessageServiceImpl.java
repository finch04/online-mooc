package com.tianji.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.message.domain.dto.ChatMessageDTO;
import com.tianji.message.domain.dto.UserInboxDTO;
import com.tianji.message.domain.po.ChatMessage;
import com.tianji.message.domain.po.MessageReceipt;
import com.tianji.message.domain.po.UserInbox;
import com.tianji.message.domain.query.ChatHistoryQuery;
import com.tianji.message.domain.vo.ChatMessageVO;
import com.tianji.message.mapper.ChatGroupMapper;
import com.tianji.message.mapper.ChatMessageMapper;
import com.tianji.message.mapper.GroupMemberMapper;
import com.tianji.message.mapper.MessageReceiptMapper;
import com.tianji.message.service.IChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    private final MessageReceiptMapper messageReceiptMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final UserClient userClient;

    @Override
    @Transactional
    public Long sendMessage(ChatMessageDTO messageDTO) {
        // 1.保存消息
        ChatMessage message = BeanUtils.copyBean(messageDTO, ChatMessage.class);
        message.setSentAt(LocalDateTime.now());
        message.setStatus(1); // 1-已发送
        save(message);

//        // 2.创建消息回执(如果是群聊，需要为每个成员创建)
//        if (messageDTO.getMessageType() == 1) { // 私聊
//            MessageReceipt receipt = new MessageReceipt()
//                    .setMessageId(message.getId())
//                    .setUserId(messageDTO.getTargetId())
//                    .setStatus(1) // 1-已发送
//                    .setUpdatedAt(LocalDateTime.now());
//            messageReceiptMapper.insert(receipt);
//        } else { // 群聊
//            // 查询群成员
//            List<Long> memberIds = groupMemberMapper.selectMemberIdsByGroupId(messageDTO.getTargetId());
//
//            List<MessageReceipt> receipts = memberIds.stream()
//                    .filter(userId -> !userId.equals(messageDTO.getSenderId())) // 不给自己发回执
//                    .map(userId -> new MessageReceipt()
//                            .setMessageId(message.getId())
//                            .setUserId(userId)
//                            .setStatus(1) // 1-已发送
//                            .setUpdatedAt(LocalDateTime.now()))
//                    .collect(Collectors.toList());
//
//            if (CollUtils.isNotEmpty(receipts)) {
//                for (MessageReceipt receipt : receipts){
//                    messageReceiptMapper.insert(receipt);
//                }
//            }
//        }

        return message.getId();
    }

    @Override
    public PageDTO<ChatMessageVO> getHistoryMessages(ChatHistoryQuery query) {
        Long userId = UserContext.getUser();


            Page<ChatMessage> page = new Page<ChatMessage>(query.getPageNo(), query.getPageSize())
                    .addOrder(new OrderItem("sent_at", false));


            if (query.getType() == 1){
                //私聊
                page = this.lambdaQuery()
                        .eq(ChatMessage::getTargetId,query.getId() ).eq(ChatMessage::getSenderId, userId)
                        .or()
                        .eq(ChatMessage::getSenderId, query.getId()).eq(ChatMessage::getTargetId, userId)
                        .page(page);
            }else if (query.getType() == 2){
                //群聊
                page = this.lambdaQuery()
                        .eq(ChatMessage::getTargetId, query.getId())
                        .page(page);
            }
            //查出发送人姓名、头像等基本信息
            List<ChatMessage> records = page.getRecords();
        // 收集发送者和接收者用户id
        Set<Long> userIds = records.stream()
                .flatMap(record -> Stream.of(record.getSenderId(), record.getTargetId()))
                .collect(Collectors.toSet());
            //根据发送者用户set用户名称和头像
            List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
            Map<Long, UserDTO> userMap = userDTOS.stream()
                    .collect(Collectors.toMap(UserDTO::getId, user -> user));
            List<ChatMessageVO> dtoList = new ArrayList<>();
            for (ChatMessage record : records) {
                ChatMessageVO dto= BeanUtils.copyProperties(record, ChatMessageVO.class);
                if(userMap.containsKey(record.getSenderId()) && record.getSenderId()!=0){
                    dto.setSenderName(userMap.get(record.getSenderId()).getName());
                    dto.setSenderIcon(userMap.get(record.getSenderId()).getIcon());
                }
                if(userMap.containsKey(record.getTargetId())){
                    dto.setTargetName(userMap.get(record.getTargetId()).getName());
                }

                dtoList.add(dto);
            }

            return PageDTO.of(page, dtoList);

    }

    @Override
    @Transactional
    public void markMessagesAsRead(List<Long> messageIds) {
        if (CollUtils.isEmpty(messageIds)) {
            return;
        }

        Long userId = UserContext.getUser();
        messageReceiptMapper.updateStatusBatch(messageIds, userId, 3, LocalDateTime.now()); // 3-已读
    }
}