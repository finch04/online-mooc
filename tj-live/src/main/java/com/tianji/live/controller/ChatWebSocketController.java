package com.tianji.live.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.tianji.live.constants.IMConstants;
import com.tianji.live.manager.ChannelIdleStateManager;
import com.tianji.live.manager.ConnectionManager;
import com.tianji.live.manager.MessageTypeDispatchManager;
import com.tianji.live.protocol.GenericMessage;
import com.tianji.live.utils.SpringContextUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * @Author: fsq
 * @Date: 2025/10/3 21:05
 * @Version: 1.0
 */
@Component
@ServerEndpoint("/chat/{userId}")
public class ChatWebSocketController {
    Logger logger = LoggerFactory.getLogger(ChatWebSocketController.class);

    //缓存当前请求的UserId
    private String userId;

    //websocket里是无法直接依赖注入的。
    private MessageTypeDispatchManager messageDispatchManager;

    private ChannelIdleStateManager channelIdleStateManager;

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        logger.info("收到新链接请求，userId={},  sessionId={}",
                userId, session.getId());
        try {//登录用户
            Long lUserId = Long.parseLong(userId);
            session.getUserProperties().put(IMConstants.PROP_USER_ID, lUserId);
            logger.info("缓存Session成功,userId={}", userId);
        } catch (NumberFormatException e) {
            //非登录用户
            logger.info("非登录用户,userId={}", userId);
        }

        //没有做去重判断，所以同一个客户端，可以重复创建连接。
//        sessionList.add(session);
//        ConnectionManager.register(session.getId(), session);
        //以UserId为key注册Session。
        if (ConnectionManager.register(userId, session)) {
            channelIdleStateManager = SpringContextUtil.getBean(ChannelIdleStateManager.class);
            channelIdleStateManager.connect(userId, session);
            logger.info("用户连接成功 userId = {}", userId);
        } else {
            logger.warn("用户已经登录，禁止重复登录！URL= {}", session.getRequestURI());
        }
        this.userId = userId;
    }

    @OnClose
    public void onClose() {
        if (null == this.userId) {
            return;
        }
        //以用户ID为Key缓存Session，自然就需要根据用户ID移除Session
        ConnectionManager.cancel(userId);
        logger.info("关闭链接");
    }

    @OnError
    public void OnError(Session session, Throwable error) {
        logger.info("链接出错" + error.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        logger.info("收到消息,message = {},sessionId = {}",message,session.getId());
        //WebSocket是多例的， 与Spring的单例是冲突的，所以无法进行依赖注入。
        //更新上次心跳时间
        if (null == channelIdleStateManager) {
            channelIdleStateManager = SpringContextUtil.getBean(ChannelIdleStateManager.class);
        }
        channelIdleStateManager.read(session);
        if(null == messageDispatchManager){
            messageDispatchManager = SpringContextUtil.getBean(MessageTypeDispatchManager.class);
        }
        //心跳请求，就直接更新请求时间，然后退出。
        if(message.equals(IMConstants.MESSAGE_HEARTBEAT)){
            return;
        }
        GenericMessage chatMessage = null;
        //消息格式不是JSON格式，更新心跳后直接退出。
        try{
            chatMessage = JSON.parseObject(message, GenericMessage.class);
        }catch(JSONException e){
            logger.error("消息格式解析错误，message={}，error={}", message, e.getMessage());
            return;
        }

        if(chatMessage.getType() == IMConstants.MESSAGE_TYPE_JOIN_ROOM
                || chatMessage.getType() == IMConstants.MESSAGE_TYPE_EXIT_ROOM){
            chatMessage.setFromUserName(chatMessage.getFromUserName());
            messageDispatchManager.messageTypeDispatch(userId,chatMessage);
        }else{
            //只有登录用户的消息进行转发
            if (session.getUserProperties().containsKey("UserId")) {
                //给当前连接推送消息
                //P5 增加消息协议定 制后，需要按照JSON格式解析消息。
                String userId = session.getUserProperties().get("UserId").toString();
                chatMessage.setFromUserId(Long.parseLong(userId));
                //根据UserId进行消息转发。转发到同一个房间的其他客户端
                messageDispatchManager.messageTypeDispatch(userId, chatMessage);
            } else {//未登录用户发消息，只给自己一个提醒。
                Optional<Session> optionalSession = ConnectionManager.getSession(this.userId);
                if (!optionalSession.isEmpty()) {
                    optionalSession.get().getBasicRemote().sendText("未登录用户不允许参与聊天，请先登录");
                }
            }
        }
    }
}
