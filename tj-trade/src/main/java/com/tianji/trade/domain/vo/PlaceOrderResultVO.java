package com.tianji.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "下单响应信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResultVO {
    @Schema(description = "订单号")
    private Long orderId;
    @Schema(description = "支付金额")
    private Integer payAmount;
    @Schema(description = "待支付的订单，超时时间")
    private LocalDateTime payOutTime;
    @Schema(description = "订单状态，1：待支付，2：已支付，3：已关闭，4：已完成，5：已报名, 6:申请退款")
    private Integer status;
}