package com.tianji.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "订单中课程信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCourseVO {
    @Schema(description = "课程id")
    private Long id;
    @Schema(description = "课程名称")
    private String name;
    @Schema(description = "课程封面url")
    private String coverUrl;
    @Schema(description = "课程价格，单位元")
    private Integer price;
}
