package com.tianji.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "下单模型")
public class PlaceOrderDTO {
    @Schema(description = "要购买的课程id列表，可以只买单个课程")
    @NotNull(message = "你还没有选好课程")
    @Size(min = 1, message = "你还没有选好课程")
    @Size(max = 10, message = "一次最多选购10门课程")
    private List<Long> courseIds;

    @Schema(description = "该订单使用的优惠券id列表，可以为空")
    private List<Long> couponIds;

    @Schema(description = "订单id")
    @NotNull(message = "订单id不能为空")
    private Long orderId;
}
