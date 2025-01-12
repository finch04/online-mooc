package com.tianji.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "退款申请数据")
public class RefundFormDTO {
    @Schema(description = "订单明细id")
    @NotNull(message = "请选中退款订单")
    private Long orderDetailId;

    @Schema(description = "退款原因")
    @NotNull(message = "请选择退款原因")
    private String refundReason;

    @Schema(description = "问题说明")
    @NotNull(message = "问题说明不能为空")
    private String questionDesc;
}
