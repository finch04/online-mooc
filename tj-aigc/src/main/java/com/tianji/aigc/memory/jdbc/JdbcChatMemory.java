package com.tianji.aigc.memory.jdbc;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianji.aigc.entity.ChatRecord;
import com.tianji.aigc.memory.MessageUtil;
import com.tianji.aigc.memory.MyChatMemory;
import com.tianji.aigc.service.ChatRecordService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

public class JdbcChatMemory implements ChatMemory, MyChatMemory {

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

        //反转对话记录
        CollUtil.reverse(chatRecordList);

        // 转为Message列表并返回
        return CollStreamUtil.toList(chatRecordList, chatRecord -> MessageUtil.toMessage(chatRecord.getData()));
    }

    @Override
    public void clear(String conversationId) {
        var queryWrapper = Wrappers.<ChatRecord>lambdaQuery().eq(ChatRecord::getConversationId, conversationId);
        this.chatRecordService.remove(queryWrapper);
    }

    /**
     * 根据对话ID优化对话记录，删除最后的2条消息，因为这2条消息是从路由智能体存储的，请求由后续的智能体处理
     * 为了确保历史消息的完整性，所以需要将中间转发的消息清理掉
     *
     * @param conversationId 对话的唯一标识符
     */
    @Override
    public void optimization(String conversationId) {
        // 构造查询条件，查询最近lastN条记录
        var queryWrapper = Wrappers.<ChatRecord>lambdaQuery()
                .eq(ChatRecord::getConversationId, conversationId)
                .orderByDesc(ChatRecord::getCreateTime)
                .last("LIMIT 2");
        // 查询数据库
        var chatRecordList = this.chatRecordService.list(queryWrapper);
        if (CollUtil.isEmpty(chatRecordList)){
            return;
        }
        //删除数据
        this.chatRecordService.removeByIds(CollStreamUtil.toList(chatRecordList,ChatRecord::getId));


    }
}
