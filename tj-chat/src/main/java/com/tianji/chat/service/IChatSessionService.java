package com.tianji.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.chat.domain.po.ChatSession;
import com.tianji.chat.domain.query.RecordQuery;
import com.tianji.common.domain.dto.PageDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * <p>
 * 聊天对话的每个片段记录（分片存储） 服务类
 * </p>
 *
 * @author lusy
 * @since 2025-05-06
 */
public interface IChatSessionService extends IService<ChatSession> {

    String chat(String sessionId, String message);

//    Flux<String> stream(String sessionId, String message);
    SseEmitter stream(String sessionId, String message);

    PageDTO<ChatSession> getRecord(RecordQuery query);

    SseEmitter fileStream(String sessionId, String message);

//    Flux<String> FileStream(String memoryId, String message);
}
