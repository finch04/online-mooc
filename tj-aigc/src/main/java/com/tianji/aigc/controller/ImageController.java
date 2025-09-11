package com.tianji.aigc.controller;

import com.tianji.aigc.domain.dto.ImageGenerateDTO;
import com.tianji.aigc.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @Author: fsq
 * @Date: 2025/9/9 14:28
 * @Version: 1.0
 */
@RestController
@RequestMapping("/img")
@RequiredArgsConstructor
public class ImageController {

    @Autowired
    private ImageService imageService;


    @PostMapping("/generate/simple")
    @Operation(summary = "简单生成图片接口（仅需提示词）")
    public String generateSimpleImage(String prompt) {
        return imageService.generateImage(prompt);
    }

    @PostMapping("/generate")
    @Operation(summary = "高级生成图片接口（支持完整参数）")
    public String generateImage(@Valid @RequestBody ImageGenerateDTO dto) {
        return imageService.generateImage(dto);
    }

}
