package com.tianji.pay.controller;


import com.tianji.common.exceptions.BadRequestException;
import com.tianji.pay.sdk.constants.PayErrorInfo;
import com.tianji.pay.sdk.constants.PayType;
import com.tianji.pay.sdk.dto.PayApplyDTO;
import com.tianji.pay.sdk.dto.PayResultDTO;
import com.tianji.pay.service.IPayOrderService;
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
 * 支付订单 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-26
 */
@Tag(name = "支付相关接口")
@RestController
@RequestMapping("/pay-orders")
@RequiredArgsConstructor
public class PayOrderController {

    private final IPayOrderService payOrderService;

    @Operation(summary = "扫码支付申请支付单，返回支付url地址，用于生产二维码")
    @PostMapping
    @ApiResponse(responseCode = "200", description = "支付URL", content = @Content(schema = @Schema(implementation = String.class)))
    public String applyPayOrder(@RequestBody @Schema(description = "支付申请信息") PayApplyDTO payApplyDTO){
        if(!PayType.NATIVE.equalsValue(payApplyDTO.getPayType())){
            throw new BadRequestException(PayErrorInfo.INVALID_PAY_TYPE);
        }
        return payOrderService.applyPayOrder(payApplyDTO);
    }

    @Operation(summary = "根据业务端订单id查询支付结果")
    @GetMapping("/{bizOrderId}/status")
    @ApiResponse(responseCode = "200", description = "支付结果", content = @Content(schema = @Schema(implementation = PayResultDTO.class)))
    public PayResultDTO queryPayResult(
            @Parameter(description = "业务订单id") @PathVariable("bizOrderId") Long bizOrderId
    ){
        return payOrderService.queryPayResult(bizOrderId);
    }
}
