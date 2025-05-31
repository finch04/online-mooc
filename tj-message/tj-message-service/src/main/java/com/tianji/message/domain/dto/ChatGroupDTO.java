package com.tianji.message.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatGroupDTO {
    private String name;
    private String tags;
    private String icon;
    private Integer status;
    private String description;
    private Long creatorId;
    private List<Long> memberIds;
}