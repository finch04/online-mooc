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
@Schema(description = "支付结果")
public class RefundResultDTO {

    public static final int RUNNING = 1;
    public static final int FAILED = 2;
    public static final int SUCCESS = 3;
    public static final String OK = "ok";

    @Schema(description = "退款状态，1：退款中，2：退款失败，3：退款成功")
    private int status;

    @Schema(description = "支付失败原因")
    private String msg;

    @Schema(description = "业务端支付订单号")
    private Long bizPayOrderId;

    @Schema(description = "业务端退款订单号")
    private Long bizRefundOrderId;

    @Schema(description = "支付流水交易单号")
    private Long payOrderNo;

    @Schema(description = "退款交易单号")
    private Long refundOrderNo;

    @Schema(description = "支付渠道")
    private String payChannel;

    @Schema(description = "退款渠道")
    private String refundChannel;

    public static RefundResultDTOBuilder success() {
        return builder().status(SUCCESS).msg(OK);
    }

    public static RefundResultDTOBuilder running() {
        return builder().status(RUNNING);
    }

    public static RefundResultDTOBuilder failed() {
        return builder().status(FAILED);
    }
}
