package com.tianji.live.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: fsq
 * @Date: 2025/10/3 16:27
 * @Version: 1.0
 */
@Data
public class MessageBody implements Serializable {

    private Long msgId;

    private String content;

    private Long userId;

    private String userName;


    @Override
    public String toString() {
        return "MessageBody{" +
                "msgId=" + msgId +
                ", content='" + content + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                '}';
    }
}
