package com.tianji.learning.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

// 分享详情DTO
@Data
public class ShareDetailDTO {
    private Long shareId;
    private String shortCode;
    private Long userId;
    private String userIcon;
    private String userName;

    private Long courseId;
    private String courseName;
    private String coverUrl;

    private Date createTime;
}