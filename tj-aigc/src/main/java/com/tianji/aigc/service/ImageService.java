package com.tianji.aigc.service;

import com.tianji.aigc.domain.dto.ImageGenerateDTO;

public interface ImageService {

    /**
     * 简单生成图片（仅需提示词）
     * @param prompt 提示词
     * @return 图片URL
     */
    String generateImage(String prompt);

    /**
     * 高级生成图片（完整参数）
     * @param dto 图片生成参数DTO
     * @return 图片URL
     */
    String generateImage(ImageGenerateDTO dto);
}
