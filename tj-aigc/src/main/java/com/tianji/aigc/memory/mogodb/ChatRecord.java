package com.tianji.aigc.memory.mogodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.index.Indexed;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("chat_record") //指定表名
public class ChatRecord {

    @Id // 标识为主键
    private ObjectId id;
    @Indexed //对话id
    private String conversationId;
    //对话数据
    private List<String> messages;

}
