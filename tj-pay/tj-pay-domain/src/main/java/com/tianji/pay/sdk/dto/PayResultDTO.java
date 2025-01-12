package com.tianji.pay.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "支付结果")
public class PayResultDTO {

    public static final int PAYING = 1;
    public static final int FAILED = 2;
    public static final int SUCCESS = 3;
    public static final String OK = "ok";

    @Schema(description = "支付结果，1：支付中，2：支付失败，3：支付成功")
    private final int status;
    @Schema(description = "支付失败原因")
    private final String msg;
    @Schema(description = "业务订单号")
    private final Long bizOrderId;
    @Schema(description = "业务订单号")
    private final Long payOrderNo;
    @Schema(description = "支付渠道")
    private final String payChannel;
    @Schema(description = "支付成功时间")
    private final LocalDateTime successTime;
}
