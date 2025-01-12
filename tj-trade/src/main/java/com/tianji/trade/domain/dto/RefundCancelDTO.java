package com.tianji.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "退款取消")
public class RefundCancelDTO {
    @Schema(description = "退款申请id，订单明细id和退款申请id二选一")
    private Long id;
    @Schema(description = "订单明细id，订单明细id和退款申请id二选一")
    private Long orderDetailId;
}
