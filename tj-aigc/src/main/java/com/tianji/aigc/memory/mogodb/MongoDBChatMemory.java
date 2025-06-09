package com.tianji.aigc.memory.mogodb;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tianji.aigc.memory.MessageUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class MongoDBChatMemory implements ChatMemory {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (CollUtil.isEmpty(messages)) {
            return;
        }

        Query query = Query.query(Criteria.where("conversationId").is(conversationId)); //构造查询条件
        ChatRecord chatRecord = this.mongoTemplate.findOne(query, ChatRecord.class);
        if (ObjectUtil.isEmpty(chatRecord)) {
            //新增
            chatRecord = ChatRecord.builder()
                    .conversationId(conversationId)
                    .messages(CollStreamUtil.toList(messages, MessageUtil::toJson))
                    .build();
        } else {
            //更新
            CollUtil.addAll(chatRecord.getMessages(), CollStreamUtil.toList(messages, MessageUtil::toJson));
        }

        // 保存数据到MongoDB
        this.mongoTemplate.save(chatRecord);

    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        return List.of();
    }

    @Override
    public void clear(String conversationId) {
        Query query = Query.query(Criteria.where("conversationId").is(conversationId)); //构造查询条件
        this.mongoTemplate.remove(query, ChatRecord.class);
    }
}
