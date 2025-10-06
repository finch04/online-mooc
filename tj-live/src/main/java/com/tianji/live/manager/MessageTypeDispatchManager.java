package com.tianji.live.manager;

import com.tianji.common.utils.StringUtils;
import com.tianji.live.constants.IMConstants;
import com.tianji.live.domain.vo.LiveRoomDetailVO;
import com.tianji.live.protocol.GenericMessage;
import com.tianji.live.protocol.MessageBody;
import com.tianji.live.service.ChatBusiService;
import com.tianji.live.service.ILiveRoomService;
import com.tianji.live.service.MessageHandlerService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Author： fsq
 * Description：消息分发服务
 **/
@Component
public class MessageTypeDispatchManager {

    private Logger logger = LoggerFactory.getLogger(MessageTypeDispatchManager.class);

    @Resource
    private MessageHandlerService messageHandlerService;

    @Resource
    private ILiveRoomService liveRoomService;

    @Resource
    private ChatBusiService chatBusiService;

    @Resource(name = "asyncExecutor")
    private Executor executor;

    /**
     * 根据UserID，对消息进行路由。
     * @param userId
     * @param message
     */
    public void messageTypeDispatch(String userId, GenericMessage message) {
        if (message.getType() == null) {
            logger.warn("消息格式异常，直接丢弃");
            return;
        }
        String roomId = message.getRoomId().toString();
        switch (message.getType()) {
            case IMConstants.MESSAGE_TYPE_JOIN_ROOM://加入房间
                ConnectionManager.joinRoom(roomId, userId);
                logger.info("用户=>{},加入房间=>{}", userId, roomId);
                // 异步获取并推送房间公告和历史消息
                executor.execute(() -> {
                    try {
                        // 获取房间信息，推送公告
                        LiveRoomDetailVO roomDetail = liveRoomService.getRoomById(Long.valueOf(roomId));
                        if (roomDetail != null && StringUtils.isNotBlank(roomDetail.getRoomDesc())) {
                            messageHandlerService.sendRoomNoticeToUser(userId, Long.valueOf(roomId), roomDetail.getRoomNotice());
                        }

                        // 获取并推送历史消息
                        List<MessageBody> historyMessages = chatBusiService.getRoomHistoryMessages(Long.valueOf(roomId));
                        if (!historyMessages.isEmpty()) {
                            messageHandlerService.sendHistoryMessagesToUser(userId, Long.valueOf(roomId), historyMessages);
                        }
                    } catch (Exception e) {
                        logger.error("推送公告或历史消息失败", e);
                    }

                    // 发送用户进入广播
                    messageHandlerService.sendIndexMessage(userId, roomId, message.getFromUserName());
                });
                break;
            case IMConstants.MESSAGE_TYPE_EXIT_ROOM://退出房间
                ConnectionManager.exitRoom(roomId, userId);
                logger.info("用户=>{},退出房间=>{}", userId, roomId);
                break;
            case IMConstants.MESSAGE_TYPE_GIFT://礼物消息(只考虑送礼物消息，和聊天消息一样转发)
            case IMConstants.MESSAGE_TYPE_CHAT://聊天
                executor.execute(() -> messageHandlerService.sendRoomChatMessage(userId, roomId, message));
                logger.info("用户=>{},房间=>{}，发送消息=>{}", userId, roomId,message);
                break;
            default:
                logger.warn("消息类型异常,message =>{}",message);
        }
    }
}
