package com.tianji.aigc.memory.clean;

import com.tianji.aigc.memory.mongodb.ChatRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MongoDataCleaner implements OriginalDataCleaner {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void clean(String conversationId) {
        Query query = Query.query(Criteria.where("conversationId").is(conversationId));
        mongoTemplate.remove(query, ChatRecord.class);
        log.info("MongoDB原始数据已删除，conversationId: {}", conversationId);
    }

    @Override
    public DataSourceType getSupportedType() {
        return DataSourceType.MONGODB;
    }
}