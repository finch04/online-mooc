package com.tianji.message.service.impl;/**
 * @author fsq
 * @date 2025/5/21 19:03
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.message.domain.dto.UserPrivateMessageFormDTO;
import com.tianji.message.domain.po.UserConversation;
import com.tianji.message.domain.po.UserPrivateMessage;
import com.tianji.message.domain.query.UserPrivateMessageQuery;
import com.tianji.message.domain.vo.UserPrivateMessageVO;
import com.tianji.message.mapper.UserPrivateMessageMapper;
import com.tianji.message.service.IUserConversationService;
import com.tianji.message.service.IUserPrivateMessageService;
import com.tianji.message.utils.SensitiveWordDetector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: fsq
 * @Date: 2025/5/21 19:03
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class UserPrivateMessageServiceImpl  extends ServiceImpl<UserPrivateMessageMapper, UserPrivateMessage> implements IUserPrivateMessageService {

    private final UserClient userClient;
    private final UserPrivateMessageMapper userPrivateMessageMapper;
//        private final IUserConversationService userConversationService;

    @Override
    public Boolean sendMessage(UserPrivateMessageFormDTO userPrivateMessageFormDTO) {
        boolean b = SensitiveWordDetector.containsSensitiveWord(userPrivateMessageFormDTO.getContent());
        if (b) {
            throw new BadRequestException("聊天消息有违禁词！");
        }
        UserPrivateMessage userPrivateMessage = new UserPrivateMessage();
        userPrivateMessage.setSenderId(UserContext.getUser());
        userPrivateMessage.setReceiverId(userPrivateMessageFormDTO.getUserId());
        userPrivateMessage.setContent(userPrivateMessageFormDTO.getContent());
        userPrivateMessage.setIsRead(0);

//        UserConversation conversation = userConversationService.getConversationByUserId(UserContext.getUser(), userPrivateMessageFormDTO.getUserId());
//        if(conversation == null){
//            //如果会话不存在，则创建会话
//            userConversationService.createConversation(UserContext.getUser(), userPrivateMessageFormDTO.getUserId());
//        }

        return this.save(userPrivateMessage);
    }

    @Override
    public PageDTO<UserPrivateMessageVO> getMessageHistory(UserPrivateMessageQuery query) {
        Long userId = UserContext.getUser();
        Page<UserPrivateMessage> page = new Page<>(query.getPageNo(), query.getPageSize());
        IPage<UserPrivateMessage> pagesList = userPrivateMessageMapper.getMessageHistory(page,query, userId);
        List<UserPrivateMessage> messagesList = pagesList.getRecords();

        if(messagesList.isEmpty()){
            return PageDTO.empty(new Page<>());
        }
        UserPrivateMessage userPrivateMessage = null;
        if(messagesList.size()>0){
            //调用用户服务给发送者和接收者添加用户信息
            userPrivateMessage = messagesList.get(0);
        }else{
            return PageDTO.empty(new Page<>());
        }



        Set<Long> userIds =new HashSet<>();
        userIds.add(userPrivateMessage.getSenderId());
        userIds.add(userPrivateMessage.getReceiverId());
//        for (UserPrivateMessage message : messagesList) {
//            userIds.add(message.getSenderId());
//            userIds.add(message.getReceiverId());
//        }
        // 批量查询用户信息
        List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
        //转成map
        Map<Long, UserDTO> userMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, userDTO -> userDTO));


        List<UserPrivateMessageVO> userPrivateMessageVOList = messagesList.stream().map(message -> {
            UserPrivateMessageVO userPrivateMessageVO = BeanUtils.toBean(message, UserPrivateMessageVO.class);

            UserDTO sender = userMap.get(message.getSenderId());
            userPrivateMessageVO.setSenderName(sender.getName());
            userPrivateMessageVO.setSenderIcon(sender.getIcon());
            UserDTO receiver = userMap.get(message.getReceiverId());
            userPrivateMessageVO.setReceiverName(receiver.getName());
            userPrivateMessageVO.setReceiverIcon(receiver.getIcon());

            //查阅到的消息标记为已读
            if(message.getReceiverId().equals(userId)){
                //必须接收者的对方才标已读
                message.setIsRead(1);
            }

            this.updateById(message);

            return userPrivateMessageVO;
        }).collect(Collectors.toList());
        return PageDTO.of(page, userPrivateMessageVOList);
    }

    //根据对方用户id得到与其最后一条消息
    @Override
    public UserPrivateMessage getLastMessage(Long otherUserId) {
        Long userId = UserContext.getUser();
        // 构建查询条件，查询两个用户之间的所有消息
        QueryWrapper<UserPrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                        .eq("sender_id", userId).eq("receiver_id", otherUserId)
                        .or()
                        .eq("sender_id", otherUserId).eq("receiver_id", userId)
                )
                .eq("delete_flag", 0) // 排除已删除的消息
                .orderByDesc("push_time") // 按发送时间降序排列
                .last("LIMIT 1"); // 只取第一条记录

        // 执行查询并返回结果
        return this.baseMapper.selectOne(queryWrapper);
    }

//    //根据对方用户id得到未读数
//    @Override
//    public Integer getUnreadCount(Long otherUserId) {
//        Long userId = UserContext.getUser();
//        // 构建查询条件，查询两个用户之间的所有消息
//        QueryWrapper<UserPrivateMessage> queryWrapper = new QueryWrapper<>();
//        queryWrapper.and(wrapper -> wrapper
//                        .eq("sender_id", userId).eq("receiver_id", otherUserId)
//                        .or()
//                        .eq("sender_id", otherUserId).eq("receiver_id", userId)
//                )
//                .eq("delete_flag", 0)
//                .eq("is_read", 0);
//        // 执行查询并返回结果
//        return this.baseMapper.selectCount(queryWrapper);
//    }

    // 根据对方用户 id 得到未读数
    @Override
    public Integer getUnreadCount(Long otherUserId) {
        Long userId = UserContext.getUser();
        // 构建查询条件，只查询对方发给当前用户且未读的消息
        QueryWrapper<UserPrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sender_id", otherUserId)
                .eq("receiver_id", userId)
                .eq("delete_flag", 0)
                .eq("is_read", 0);
        // 执行查询并返回结果
        return this.baseMapper.selectCount(queryWrapper);
    }

    //得到自己总的私信未读数
    @Override
    public Integer getAllUnreadCountBySelf(Long userId) {
        // 构建查询条件，只查询对方发给当前用户且未读的消息
        QueryWrapper<UserPrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("receiver_id", userId)
                .eq("delete_flag", 0)
                .eq("is_read", 0);
        // 执行查询并返回结果
        return this.baseMapper.selectCount(queryWrapper);
    }

}