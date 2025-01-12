package com.tianji.pay.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "退款请求参数")
public class RefundApplyDTO {

    @Schema(description = "支付时传入的业务订单id")
    private Long bizOrderNo;

    @Schema(description = "本次要退款的业务订单id，因为有拆单，这里是子订单id")
    private Long bizRefundOrderNo;

    @Schema(description = "子订单的退款金额,单位为分")
    private Integer refundAmount;
}
