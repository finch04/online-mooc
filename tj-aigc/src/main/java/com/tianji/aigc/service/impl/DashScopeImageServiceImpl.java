package com.tianji.aigc.service.impl;

import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import com.tianji.aigc.entity.dto.ImageGenerateDTO;
import com.tianji.aigc.enums.ImageStyleEnum;
import com.tianji.aigc.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashScopeImageServiceImpl implements ImageService {

    private final DashScopeImageModel dashScopeImageModel;

    // 阿里文生图模型常量
    private static final String DEFAULT_MODEL = "wanx-v1";
    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = 1024;

    @Override
    public String generateImage(String prompt) {
        // 构建默认DTO
        ImageGenerateDTO dto = new ImageGenerateDTO();
        dto.setPrompt(prompt);
        return generateImage(dto);
    }

    /**
     * 通过DTO参数生成图片
     * @param dto 图片生成参数DTO
     * @return 生成的图片URL
     */
    @Override
    public String generateImage(ImageGenerateDTO dto) {
        try {
            // 处理默认值
            ImageStyleEnum style = dto.getStyle() != null ? dto.getStyle() : ImageStyleEnum.AUTO;
            int width = dto.getWidth() != null ? dto.getWidth() : DEFAULT_WIDTH;
            int height = dto.getHeight() != null ? dto.getHeight() : DEFAULT_HEIGHT;
//            int count = dto.getCount() > 0 ? dto.getCount() : 1;

            // 构建阿里文生图特定参数
            DashScopeImageOptions options = DashScopeImageOptions.builder()
                    .withModel(DEFAULT_MODEL)
                    .withStyle(style.getCode())
                    .withWidth(width)
                    .withHeight(height)
                    .withN(1)
                    .build();

            // 构建图片生成提示
            ImagePrompt imagePrompt = new ImagePrompt(dto.getPrompt(), options);

            // 调用模型生成图片
            ImageResponse response = dashScopeImageModel.call(imagePrompt);

            // 解析响应获取图片URL
            if (response.getResults() != null && !response.getResults().isEmpty()) {
                String imageUrl = response.getResults().get(0).getOutput().getUrl();
                log.info("图片生成成功，URL: {}", imageUrl);
                return imageUrl;
            }

            log.error("图片生成失败，未返回有效结果");
            throw new RuntimeException("图片生成失败，未获取到图片URL");

        } catch (Exception e) {
            log.error("调用阿里文生图模型失败", e);
            throw new RuntimeException("图片生成处理失败: " + e.getMessage());
        }
    }
}
