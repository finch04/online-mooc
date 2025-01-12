package com.tianji.trade.domain.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "订单分页信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageVO {
    @Schema(description = "订单id")
    private Long id;
    @Schema(description = "订单创建时间")
    private LocalDateTime createTime;
    @Schema(description = "订单实付金额")
    private Integer realAmount;
    @Schema(description = "订单明细金额")
    private Integer totalAmount;
    @Schema(description = "订单状态1：待支付，2：已支付，3：已关闭，4：已完成，5：已报名")
    private Integer status;
    @Schema(description = "订单状态描述")
    private String statusDesc;
    @Schema(description = "订单中课程明细")
    private List<OrderDetailVO> details;
}
