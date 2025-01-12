package com.tianji.api.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分类id和名称信息")
public class CategoryBasicDTO {
    @Schema(description = "分类id", example = "1")
    private Long id;
    @Schema(description = "分类名称", example = "Java")
    private String name;
    @Schema(description = "父分类id", example = "0")
    private Long parentId;
}
