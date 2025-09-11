package com.tianji.aigc.memory.jdbc;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianji.aigc.domain.ChatRecord;
import com.tianji.aigc.memory.MessageUtil;
import com.tianji.aigc.memory.MyChatMemoryRepository;
import com.tianji.aigc.service.ChatRecordService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 基于JDBC的聊天记忆存储
 */
public class JdbcChatMemoryRepository implements ChatMemoryRepository, MyChatMemoryRepository {

    @Resource
    private ChatRecordService chatRecordService;

    @Override
    public List<String> findConversationIds() {
        var queryWrapper = new QueryWrapper<ChatRecord>();
        queryWrapper.select("DISTINCT conversationId");
        var chatRecordList = this.chatRecordService.list(queryWrapper);
        return CollStreamUtil.toList(chatRecordList, ChatRecord::getConversationId);
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        var chatRecordList = this.chatRecordService.lambdaQuery()
                .eq(ChatRecord::getConversationId, conversationId)
                .orderByAsc(ChatRecord::getCreateTime)
                .list();
        return CollStreamUtil.toList(chatRecordList, chatRecord -> MessageUtil.toMessage(chatRecord.getData()));
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        // 先删除原有数据
        this.deleteByConversationId(conversationId);
        // 通过对话id获取用用户id
        var userId = Convert.toLong(StrUtil.subBefore(conversationId, '_', true));

        // 批量保存数据到数据库
        var chatRecordList = CollStreamUtil.toList(messages, message -> ChatRecord.builder()
                .data(MessageUtil.toJson(message))
                .conversationId(conversationId)
                .creater(userId)
                .updater(userId)
                .build());
        this.chatRecordService.saveBatch(chatRecordList);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        var queryWrapper = Wrappers.<ChatRecord>lambdaQuery()
                .eq(ChatRecord::getConversationId, conversationId);
        this.chatRecordService.remove(queryWrapper);
    }

    @Override
    public void optimization(String conversationId) {
        // 1. 查询当前对话的最新2条消息（按创建时间倒序取前2条）
        List<ChatRecord> latestRecords = this.chatRecordService.lambdaQuery()
                .eq(ChatRecord::getConversationId, conversationId)
                .orderByDesc(ChatRecord::getCreateTime)
                .last("LIMIT 2") // 取最新的2条
                .list();

        if (latestRecords.isEmpty()) {
            return; // 无记录则无需处理
        }

        // 2. 删除这2条消息
        List<Long> ids = CollStreamUtil.toList(latestRecords, ChatRecord::getId);
        this.chatRecordService.removeByIds(ids);
    }
}
