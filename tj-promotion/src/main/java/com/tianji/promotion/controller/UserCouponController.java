package com.tianji.promotion.controller;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.promotion.domain.dto.CouponDiscountDTO;
import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.dto.OrderCouponDTO;
import com.tianji.promotion.domain.dto.OrderCourseDTO;
import com.tianji.promotion.domain.query.UserCouponQuery;
import com.tianji.promotion.domain.vo.CouponPageVO;
import com.tianji.promotion.domain.vo.CouponVO;
import com.tianji.promotion.enums.DiscountType;
import com.tianji.promotion.enums.ObtainType;
import com.tianji.promotion.service.ICouponService;
import com.tianji.promotion.service.IDiscountService;
import com.tianji.promotion.service.IUserCouponService;
import io.seata.spring.annotation.GlobalTransactional;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 前端控制器
 * </p>
 *
 * @author fsq
 * @since 2023-10-29
 */
@Api(tags = "用户券相关接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user-coupons")
public class UserCouponController {

    private final IUserCouponService userCouponService;
    private final IDiscountService discountService;

    @ApiOperation("领取优惠券")
    @PostMapping("{id}/receive")
    public void receiveCoupon(@PathVariable Long id){
        userCouponService.receiveCoupon(id);
    }

    @ApiOperation("兑换码兑换优惠券")
    @PostMapping("{code}/exchange")
    public void exchangeCoupon(@PathVariable String code){
        userCouponService.exchangeCoupon(code);
    }

    @ApiOperation("查询我的优惠券")
    @GetMapping("page")
    public PageDTO<CouponVO> queryMyCouponPage(UserCouponQuery query){
        return userCouponService.queryMyCouponPage(query);
    }

    //该方法是给tj-trade远程调用的
    @ApiOperation("查询可用优惠券方案")
    @PostMapping("available")
    public List<CouponDiscountDTO> findDiscountSolution(@RequestBody List<OrderCourseDTO> dto){
        return userCouponService.findDiscountSolution(dto);
    }

    @ApiOperation("根据券方案计算订单优惠明细")
    @PostMapping("/discount")
    public CouponDiscountDTO queryDiscountDetailByOrder(
            @RequestBody OrderCouponDTO orderCouponDTO){
        return discountService.queryDiscountDetailByOrder(orderCouponDTO);
    }

    @ApiOperation("核销指定优惠券")
    @PutMapping("/use")
    public void writeOffCoupon(@ApiParam("用户优惠券id集合") @RequestParam("couponIds") List<Long> userCouponIds){
        userCouponService.writeOffCoupon(userCouponIds);
    }

    @ApiOperation("退还指定优惠券")
    @PutMapping("/refund")
    public void refundCoupon(@ApiParam("用户优惠券id集合") @RequestParam("couponIds") List<Long> userCouponIds){
        userCouponService.refundCoupon(userCouponIds);
    }

    @ApiOperation("分页查询我的优惠券接口")
    @GetMapping("/rules")
    public List<String> queryDiscountRules(
            @ApiParam("用户优惠券id集合") @RequestParam("couponIds") List<Long> userCouponIds){
        return userCouponService.queryDiscountRules(userCouponIds);
    }

    @ApiOperation("优惠券模板id转化为用户优惠券id")
    @GetMapping("transform")
    public List<Long> transformCouponIds(
            @ApiParam("优惠券模板id集合") @RequestParam("couponIds") List<Long> couponIds){
        return userCouponService.transformCouponIds(couponIds);
    }




////    测试接口-以下均为测试
//    @Autowired
//    private LearningClient learningClient;
//    private final ICouponService couponService;
//
//
//    /**
//     * 测试dubbo
//     */
//    @GetMapping("/hello")
//    public void sayhello() throws InterruptedException {
//        String ans = learningClient.sayHello("hello");
//        System.out.println("ans = " + ans);
//    }
//
//    /**
//     * 测试senta分布式事务
//     */
//    @PostMapping("/seata")
//    @GlobalTransactional(rollbackFor = Exception.class)
//    public void seata() {
//        CouponFormDTO dto = new CouponFormDTO();
//        dto.setId(1799677743084539999L);
//        dto.setName("测试优惠券");
//        dto.setDiscountType(DiscountType.PRICE_DISCOUNT);
//        dto.setSpecific(false);
//        dto.setDiscountValue(1000);
//        dto.setThresholdAmount(100000);
//        dto.setMaxDiscountAmount(0);
//        dto.setObtainWay(ObtainType.PUBLIC);
//        couponService.saveCoupon(dto);
//        learningClient.testSeata();
//    }
//
//    /**
//     * 测试sentinel限流
//     *
//     * @return
//     */
//    @GetMapping("/limit")
//    @SentinelResource(value = "QUEUE-DATA-FLOW")
//    public String limit() {
//        return "hello";
//    }

}
