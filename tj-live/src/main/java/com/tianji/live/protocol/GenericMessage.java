package com.tianji.live.protocol;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/10/3 16:27
 * @Version: 1.0
 */
@Data
public class GenericMessage implements Serializable {

    /**
     * 消息类型：
     */
    private Integer type;

    /**
     * 房间ID
     */
    private Long roomId;

    private Long fromUserId;

    private String fromUserName;

    /**
     * 消息体
     */
    private List<MessageBody> body;


    @Override
    public String toString() {
        return "GenericMessage{" +
                "type=" + type +
                ", roomId=" + roomId +
                ", fromUserId=" + fromUserId +
                ", fromUserName='" + fromUserName + '\'' +
                ", body=" + body +
                '}';
    }
}
