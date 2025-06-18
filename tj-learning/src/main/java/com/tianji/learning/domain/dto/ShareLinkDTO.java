package com.tianji.learning.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// 分享链接DTO
@Data
@AllArgsConstructor
public class ShareLinkDTO {
    private Long userId;
    private Long courseId;
    private String shortUrl;
//    private String qrCodeUrl;
}
