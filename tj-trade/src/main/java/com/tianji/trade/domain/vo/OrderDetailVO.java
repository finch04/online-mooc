package com.tianji.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "订单条目中的课程信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO {
    @Schema(description = "订单条目id")
    private Long id;
    @Schema(description = "总订单id")
    private Long orderId;
    @Schema(description = "课程id")
    private Long courseId;
    @Schema(description = "课程名称")
    private String name;
    @Schema(description = "封面")
    private String coverUrl;
    @Schema(description = "课程价格")
    private Integer price;
    @Schema(description = "实付金额")
    private Integer realPayAmount;
    @Schema(description = "退款状态")
    private Integer refundStatus;
    @Schema(description = "优惠券规则")
    private String couponDesc;
    @Schema(description = "是否可以退款")
    private Boolean canRefund;
}