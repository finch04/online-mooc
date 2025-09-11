package com.tianji.aigc.domain.dto;

import com.tianji.aigc.enums.ImageStyleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;


/**
 * 图片生成请求DTO
 */
@Data
public class ImageGenerateDTO {

    @NotEmpty(message = "提示词不能为空")
    @Schema(description = "提示词", example = "一个中国小城")
    private String prompt;

    @Schema(description = "图片风格", example = "PHOTOGRAPHY", type= "可选值: AUTO, PHOTOGRAPHY, PORTRAIT, CARTOON_3D, ANIME, OIL_PAINTING, WATERCOLOR, SKETCH, CHINESE_PAINTING, FLAT_ILLUSTRATION")
    private ImageStyleEnum style = ImageStyleEnum.AUTO;

    @Positive(message = "宽度必须为正数")
    private Integer width = 1024;

    @Positive(message = "高度必须为正数")
    private Integer height = 1024;
}