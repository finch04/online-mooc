package com.tianji.promotion.service.impl;

import com.tianji.promotion.domain.dto.CouponDiscountDTO;

import com.tianji.common.utils.CollUtils;
import com.tianji.promotion.domain.dto.OrderCouponDTO;

import com.tianji.promotion.domain.dto.OrderCourseDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.enums.UserCouponStatus;
import com.tianji.promotion.mapper.UserCouponMapper;

import com.tianji.promotion.service.ICouponScopeService;
import com.tianji.promotion.service.IDiscountService;
import com.tianji.promotion.service.IUserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements IDiscountService {

    private final UserCouponMapper userCouponMapper;

    private final IUserCouponService userCouponService;


    @Override
    public CouponDiscountDTO queryDiscountDetailByOrder(OrderCouponDTO orderCouponDTO) {
        // 1.查询用户优惠券
        //TODO 这里OrderCouponDTO传入的并不是用户优惠券id，而是优惠券模板id，不要搞混！！
        List<Long> userCouponIds = orderCouponDTO.getUserCouponIds();
        List<Coupon> coupons = userCouponMapper.queryCouponByUserCouponIds(userCouponIds, UserCouponStatus.UNUSED);
        if (CollUtils.isEmpty(coupons)) {
            return null;
        }
        // 2.查询优惠券对应课程
        Map<Coupon, List<OrderCourseDTO>> availableCouponMap = userCouponService.findAvailableCoupons(coupons, orderCouponDTO.getCourseList());
        if (CollUtils.isEmpty(availableCouponMap)) {
            return null;
        }
        // 3.查询优惠券规则
        CouponDiscountDTO couponDiscountDTO = userCouponService.calculateSolutionDiscount(availableCouponMap, orderCouponDTO.getCourseList(), coupons);
        List<Long> longs = userCouponService.transformCouponIds(couponDiscountDTO.getIds());
        couponDiscountDTO.setIds(longs);
        return couponDiscountDTO;
    }

}
