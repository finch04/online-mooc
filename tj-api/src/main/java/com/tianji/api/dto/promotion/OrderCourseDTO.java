package com.tianji.api.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "订单中的课程信息")
public class OrderCourseDTO {
    @Schema(description = "课id")
    private Long id;
    @Schema(description = "课程的三级分类id")
    private Long cateId;
    @Schema(description = "课程价格")
    private Integer price;
}
