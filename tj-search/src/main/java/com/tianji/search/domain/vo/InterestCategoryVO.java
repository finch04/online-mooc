package com.tianji.search.domain.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "感兴趣的分类")
public class InterestCategoryVO {
    @Schema(description = "分类id", example = "1")
    private Long id;
    @Schema(description = "分类名称", example = "Java")
    private String name;
}
