package com.tianji.aigc.memory;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianji.aigc.entity.ChatRecord;
import com.tianji.aigc.service.ChatRecordService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

public class JdbcChatMemory implements ChatMemory {

    @Resource
    private ChatRecordService chatRecordService;

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (CollUtil.isEmpty(messages)) {
            return;
        }

        // 获取用户ID
        Long userId = Convert.toLong(StrUtil.subBefore(conversationId, "_", true));

        // 将messages转为ChatRecord列表
        List<ChatRecord> chatRecordList = CollStreamUtil.toList(messages, message -> ChatRecord.builder()
                .conversationId(conversationId)
                .data(MessageUtil.toJson(message))
                .creater(userId)
                .updater(userId)
                .build());

        // 批量保存到数据库中
        this.chatRecordService.saveBatch(chatRecordList);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        if (lastN <= 0) {
            return List.of();
        }
        // 构造查询条件，查询最近lastN条记录
        var queryWrapper = Wrappers.<ChatRecord>lambdaQuery()
                .eq(ChatRecord::getConversationId, conversationId)
                .orderByDesc(ChatRecord::getCreateTime)
                .last("LIMIT " + lastN);

        // 查询数据库
        var chatRecordList = this.chatRecordService.list(queryWrapper);

        // 转为Message列表并返回
        return CollStreamUtil.toList(chatRecordList, chatRecord -> MessageUtil.toMessage(chatRecord.getData()));
    }

    @Override
    public void clear(String conversationId) {
        var queryWrapper = Wrappers.<ChatRecord>lambdaQuery().eq(ChatRecord::getConversationId, conversationId);
        this.chatRecordService.remove(queryWrapper);
    }
}
