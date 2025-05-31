package com.tianji.message.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class ChatGroupVO {
    private Long id;
    private String name;
    private String tags;
    private String icon;
    private String description;
    private Integer status;
    private Long creatorId;
    private Integer memberCount;
    private Boolean isJoined;
}