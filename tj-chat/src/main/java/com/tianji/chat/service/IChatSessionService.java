package com.tianji.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.chat.domain.po.ChatSession;
import reactor.core.publisher.Flux;

/**
 * <p>
 * 聊天对话的每个片段记录（分片存储） 服务类
 * </p>
 *
 * @author lusy
 * @since 2025-05-06
 */
public interface IChatSessionService extends IService<ChatSession> {

    String chat(String memoryId, String message);

    Flux<String> stream(String memoryId, String message);

//    Flux<String> FileStream(String memoryId, String message);
}
