package com.tianji.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "订单明细分页结果")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailPageVO {
    @Schema(description = "订单明细id")
    private Long id;
    @Schema(description = "订单id")
    private Long orderId;
    @Schema(description = "学员姓名")
    private String name;
    @Schema(description = "手机号")
    private String mobile;
    @Schema(description = "订单金额，也就是课程原价")
    private Integer price;
    @Schema(description = "实付金额")
    private Integer realPayAmount;
    @Schema(description = "订单状态1：待支付，2：已支付，3：已关闭，4：已完成，5：已报名")
    private Integer status;
    @Schema(description = "订单状态描述")
    private String statusDesc;
    @Schema(description = "退款状态1：待支付，2：已支付，3：已关闭，4：已完成，5：已报名，0：表示没有退款状态")
    private Integer refundStatus;
    @Schema(description = "退款状态描述")
    private String refundStatusDesc;
    @Schema(description = "订单时间")
    private LocalDateTime createTime;
    @Schema(description = "支付方式")
    private String payChannel;
}