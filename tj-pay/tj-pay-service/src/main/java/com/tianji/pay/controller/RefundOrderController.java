package com.tianji.pay.controller;


import com.tianji.pay.sdk.dto.RefundApplyDTO;
import com.tianji.pay.sdk.dto.RefundResultDTO;
import com.tianji.pay.service.IRefundOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 退款订单 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-26
 */
@Tag(name = "支付相关接口")
@RestController
@RequestMapping("/refund-orders")
@RequiredArgsConstructor
public class RefundOrderController {

    private final IRefundOrderService refundOrderService;

    @Operation(summary = "申请退款接口")
    @PostMapping
    @ApiResponse(responseCode = "200", description = "退款申请结果", content = @Content(schema = @Schema(implementation = RefundResultDTO.class)))
    public RefundResultDTO applyRefund(@RequestBody @Schema(description = "退款申请信息") RefundApplyDTO refundApplyDTO) {
        return refundOrderService.applyRefund(refundApplyDTO);
    }

    @Operation(summary = "查询退款结果")
    @GetMapping("{bizRefundOrderId}/status")
    @ApiResponse(responseCode = "200", description = "退款结果", content = @Content(schema = @Schema(implementation = RefundResultDTO.class)))
    public RefundResultDTO queryRefundResult(
            @Parameter(description = "业务端退款的子订单id") @PathVariable("bizRefundOrderId") Long bizRefundOrderId) {
        return refundOrderService.queryRefundResult(bizRefundOrderId);
    }

/*    @GetMapping("{bizRefundOrderId}/detail")
    @ApiOperation("查询退款详情")
    public RefundResultDTO queryRefundDetail(
            @ApiParam("业务端退款的子订单id") @PathVariable("bizRefundOrderId") Long bizRefundOrderId) {
        return refundOrderService.queryRefundDetail(bizRefundOrderId);
    }*/
}
