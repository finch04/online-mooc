package com.tianji.live.service;

import com.tianji.api.client.unqid.UnqidClient;
import com.tianji.live.constants.IMConstants;
import com.tianji.live.protocol.GenericMessage;
import com.tianji.live.protocol.MessageBody;
import com.tianji.live.utils.IMCacheKeyBuilder;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Author： roy
 * Description：对消息做处理--简单实现
 **/
@Service
public class ChatBusiService {

    private Logger logger = LoggerFactory.getLogger(ChatBusiService.class);

    @Value("${tl-live.message.cachesize:5}")
    private int roomMessageCacheSize;
    @Resource
    private RedisTemplate<String, MessageBody> redisTemplate;
    @Resource
    private IIMRPCService imRPCService;
    @Resource
    private UnqidClient unqidClient;
    @Resource
    private IMCacheKeyBuilder imCacheKeyBuilder;

    /**
     * 对消息做处理。真实业务中应包含消息缓存、消息过滤、敏感信息隐藏、消息聚合等。
     * 这里简单实现，将消息缓存到Redis，然后每个房间积累到5条，就往外进行一次推送。
     * @param message：接收到的上行消息
     */
    public void handleMessage(GenericMessage message){
        //通过Dubbo服务将处理完的消息推送到IMServer
        logger.info("处理消息："+message);
        Long roomId = message.getRoomId();

        String cacheKey = imCacheKeyBuilder.buildRoomMessageCacheKey(roomId);

        //礼物消息不缓存，直接推送
        if(message.getType().equals(IMConstants.MESSAGE_TYPE_GIFT)){
            imRPCService.pushChatMessage(roomId,message);
        //聊天消息，缓存Redis，聚合一定条数后再推送。
        }else{
            for (MessageBody messageBody : message.getBody()) {
                //需要给每个消息生成一个ID，防止用户多次发送相同内容时，被认为是一条重复的消息
                messageBody.setMsgId(unqidClient.getUnSeqId());
                //消息是以房间为单位聚合，一批消息中包含多人发送的消息，所以要将消息发送者放到MessageBody中。
                messageBody.setUserId(message.getFromUserId());
                messageBody.setUserName(message.getFromUserName());
                redisTemplate.opsForSet().add(cacheKey,messageBody);
            }

            Set<MessageBody> roomMessages = redisTemplate.opsForSet().members(cacheKey);
            if(roomMessages.size() > roomMessageCacheSize){
                logger.info("房间 {} 缓存消息数量超过 {} 条，准备进行推送",roomId,roomMessageCacheSize);
                //组装下行GenericMessage,往IM推送消息
                GenericMessage downMessage = new GenericMessage();
                downMessage.setRoomId(roomId);
                downMessage.setBody(roomMessages.stream().toList());
                downMessage.setType(IMConstants.MESSAGE_TYPE_CHAT);
                try{
                    imRPCService.pushChatMessage(roomId,downMessage);
                    logger.info("消息 {} 下发所有链接服务器成功",downMessage);
                }catch (Exception e){
                    logger.error("消息 {} 推送失败，可能造成消息丢失。失败原因：{}",downMessage,e.getMessage());
                }
                //清理redis缓存
                redisTemplate.opsForSet().remove(cacheKey,roomMessages.toArray());
            }
        }
    }
}
