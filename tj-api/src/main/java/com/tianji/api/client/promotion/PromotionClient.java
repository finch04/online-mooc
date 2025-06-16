package com.tianji.api.client.promotion;


import com.tianji.api.client.promotion.fallback.PromotionClientFallback;
import com.tianji.api.dto.promotion.CouponDetailSimpleVO;
import com.tianji.api.dto.promotion.CouponDiscountDTO;
import com.tianji.api.dto.promotion.OrderCouponDTO;
import com.tianji.api.dto.promotion.OrderCourseDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//促销服务 feign客户端

@FeignClient(value = "promotion-service",fallbackFactory = PromotionClientFallback.class)
public interface PromotionClient {

    @ApiOperation("查询优惠券方案列表")
    @PostMapping("/user-coupons/available")
    public List<CouponDiscountDTO> findDiscountSolution(@RequestBody List<OrderCourseDTO> courses);

    @ApiOperation("根据id查询优惠券接口")
    @GetMapping("/coupons/simple/{id}")
    public CouponDetailSimpleVO querySimpleCouponById(@PathVariable("id") Long id);

    @ApiOperation("根据券方案计算订单优惠明细")
    @PostMapping("/user-coupons/discount")
    CouponDiscountDTO queryDiscountDetailByOrder(@RequestBody OrderCouponDTO orderCouponDTO);

    @ApiOperation("核销指定优惠券")
    @PutMapping("/user-coupons/use")
    void writeOffCoupon(@ApiParam("用户优惠券id集合") @RequestParam("couponIds") List<Long> userCouponIds);

    @ApiOperation("退还指定优惠券")
    @PutMapping("/user-coupons/refund")
    void refundCoupon(@ApiParam("用户优惠券id集合") @RequestParam("couponIds") List<Long> userCouponIds);

    @ApiOperation("分页查询我的优惠券接口")
    @GetMapping("/user-coupons/rules")
    List<String> queryDiscountRules(@ApiParam("用户优惠券id集合") @RequestParam("couponIds") List<Long> userCouponIds);

    @ApiOperation("优惠券模板id转化为用户优惠券id")
    @GetMapping("/user-coupons/transform")
    List<Long> transformCouponIds(@ApiParam("优惠券模板id集合") @RequestParam("couponIds") List<Long> couponIds);
}
