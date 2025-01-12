package com.tianji.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "退款信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundApplyPageVO {
    @Schema(description = "退款id")
    private Long id;
    @Schema(description = "订单明细id")
    private Long orderDetailId;
    @Schema(description = "订单id")
    private Long orderId;
    @Schema(description = "退款金额")
    private Integer refundAmount;
    @Schema(description = "申请人")
    private String proposerName;
    @Schema(description = "申请人手机号")
    private String proposerMobile;
    @Schema(description = "退款申请状态")
    private Integer status;
    @Schema(description = "退款申请状态描述")
    private String refundStatusDesc;
    @Schema(description = "退款申请时间")
    private LocalDateTime createTime;

    @Schema(description = "审批人")
    private String approverName;
    @Schema(description = "审批时间")
    private String approveTime;

    @Schema(description = "退款成功时间")
    private LocalDateTime refundSuccessTime;
}