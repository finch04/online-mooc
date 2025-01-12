package com.tianji.trade.domain.vo;

import com.tianji.api.dto.promotion.CouponDiscountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Schema(description = "订单确认页信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmVO {
    @Schema(description = "订单id")
    private Long orderId;
    @Schema(description = "订单总金额")
    private Integer totalAmount;
    @Schema(description = "优惠折扣方案")
    private List<CouponDiscountDTO> discounts;
    @Schema(description = "订单中包含的课程")
    private List<OrderCourseVO> courses;
}