package com.tianji.live.service;

import com.alibaba.fastjson.JSON;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.utils.StringUtils;
import com.tianji.live.constants.IMConstants;
import com.tianji.live.manager.ConnectionManager;
import com.tianji.live.protocol.GenericMessage;
import com.tianji.live.protocol.MessageBody;
import com.tianji.live.utils.IMCacheKeyBuilder;
import jakarta.annotation.Resource;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

import static com.tianji.common.constants.MqConstants.Exchange.LIVE_EXCHANGE;

/**
 * Author： roy
 * Description：消息推送服务
 **/
@Component
public class MessageHandlerService {

    private Logger logger = LoggerFactory.getLogger(MessageHandlerService.class);
    @Resource(name = "asyncExecutor")
    private Executor executor;

    @Resource
    private RabbitMqHelper rabbitMqHelper;

    @Resource
    private IMCacheKeyBuilder imCacheKeyBuilder;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    public void sendIndexMessage(String userId, String roomId,String userName) {
        Optional<Session> connOpt = ConnectionManager.getSession(userId);
        if (!connOpt.isPresent()) {
            logger.info("用户路由不存在,首页消息推送失败,userId=>{}", userId);
            return;
        }
        Session socketSession = connOpt.get();
        if (!socketSession.isOpen()) {
            logger.info("用户已经断开连接,首页消息推送失败,userId=>{}", userId);
        }
        if(StringUtils.isNotEmpty(userName)){
            GenericMessage broadcastMsg = new GenericMessage();
            broadcastMsg.setType(IMConstants.MESSAGE_TYPE_JOIN_ROOM);
            broadcastMsg.setRoomId(Long.valueOf(roomId));

            MessageBody body = new MessageBody();
            body.setContent( userName + " 进入了直播间");

            List<MessageBody> bodies = new ArrayList<>();
            bodies.add(body);
            broadcastMsg.setBody(bodies);
            sendRoomBroadCast(Long.valueOf(roomId), broadcastMsg);
        }

//         查询房间最新20条消息，推送给用户，没有存储消息就不实现了

//        logger.info("异步推送首页消息成功！！！！！！！！！！");
    }

    /**
     * 转发聊天消息
     * @param userId
     * @param roomId
     * @param message
     */
    public void sendRoomChatMessage(String userId, String roomId, GenericMessage message) {

//        List<Session> allUserConnect = ConnectionManager.getRoomAllUserConnect(roomId, userId);
//        if (allUserConnect.isEmpty()) {
//            return;
//        }
//        // 消息发送需要根据房间中用户，进行消息分裂。
//        allUserConnect.forEach(c -> executor.execute(() -> sendMessage(c, message)));
//
        message.setFromUserId(Long.parseLong(userId));
        logger.info("消息准备发送");
//        //通过MQ往后端转发消息
        rabbitMqHelper.send(LIVE_EXCHANGE, MqConstants.Key.LIVE_IM_MESSAGE, message);
//        chatBusiService.handleMessage(message);
        logger.info("消息异步分发成功");
    }

    public boolean sendRoomBroadCast(Long roomId, GenericMessage message){
        List<Session> allUserConnect = ConnectionManager.getRoomAllUserConnect(roomId.toString());
        if (allUserConnect.isEmpty()) {
            return false;
        }
        // 消息发送需要根据房间中用户，进行消息分裂。
        allUserConnect.forEach(c -> executor.execute(() -> sendMessage(c, message)));
        logger.info("直播间公告异步分发成功");
        return true;
    }

    public void sendMessage(Session session, GenericMessage message) {
        if (session.isOpen()) {
            try {
//                Object userId = session.getUserProperties().get(IMConstants.PROP_USER_ID);
//                message.setFromUserId(Long.parseLong(userId.toString()));
                session.getBasicRemote().sendText(JSON.toJSONString(message));
            } catch (IOException e) {
                logger.error("发送消息失败,用户信息=>{},message=>{}", session.getUserProperties().get("UserId"), message);
            }
        }
    }

    /**
     * 往房间推送聊天消息
     * @param roomId
     * @param genericMessage 包含同一个房间内的多条消息。消息的发送者ID，在MessageBody中。
     * @return
     */
    public boolean pushChatMessage(Long roomId, GenericMessage genericMessage) {
        //从Redis中获取房间内的所有用户信息
        String roomUserCacheKey = imCacheKeyBuilder.buildRoomUserCacheKey(roomId.toString());
        Set<String> roomUsers = stringRedisTemplate.opsForSet().members(roomUserCacheKey);
        //依次检查本地是否有用户对应的Session
        roomUsers.forEach(userId -> {
            Optional<Session> connOpt = ConnectionManager.getSession(userId);
            if(connOpt.isPresent()){//有的话就推送消息
                sendMessage(connOpt.get(),genericMessage);
            }
            //如果没有，表示用户对应的Session不在当前服务上，而是在集群中的其他服务上。
        });
        return true;
    }
}
