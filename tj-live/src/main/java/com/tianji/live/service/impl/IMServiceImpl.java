package com.tianji.live.service.impl;

import com.tianji.live.constants.IMConstants;
import com.tianji.live.protocol.GenericMessage;
import com.tianji.live.protocol.MessageBody;
import com.tianji.live.service.IIMService;
import com.tianji.live.handler.MessageHandlerService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IMServiceImpl implements IIMService {

    private Logger logger = LoggerFactory.getLogger(IMServiceImpl.class);

    @Resource
    private MessageHandlerService messageHandlerService;

    @Override
    public boolean sendMesasgeToRoom(Long roomId, String message) {
        GenericMessage genericMessage = new GenericMessage();
        genericMessage.setRoomId(roomId);
        genericMessage.setType(IMConstants.MESSAGE_TYPE_NOTICE);

        List<MessageBody> messageBodies = new ArrayList<>();

        MessageBody messageBody = new MessageBody();
        messageBody.setContent(message);
        messageBodies.add(messageBody);

        genericMessage.setBody(messageBodies);
        return messageHandlerService.sendRoomBroadCast(roomId,genericMessage);
    }

    @Override
    public boolean pushChatMessage(Long roomId, GenericMessage genericMessage) {
        return messageHandlerService.pushChatMessage(roomId,genericMessage);
    }
}
