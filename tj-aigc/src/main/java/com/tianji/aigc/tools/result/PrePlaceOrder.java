package com.tianji.aigc.tools.result;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.tianji.api.dto.promotion.CouponDiscountDTO;
import com.tianji.api.dto.promotion.OrderCourseDTO;
import com.tianji.api.dto.trade.OrderConfirmVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrePlaceOrder {

    @JsonPropertyDescription("课程数量")
    private int count;

    @JsonPropertyDescription("订单总金额")
    private double totalAmount;

    @JsonPropertyDescription("最大优惠金额")
    private double discountAmount;

    @JsonPropertyDescription("优惠券名称")
    private String couponName;

    @JsonPropertyDescription("实付金额")
    private double payAmount;

    @JsonPropertyDescription("课程id列表")
    private List<Long> courseIds;

    @JsonPropertyDescription("订单id")
    private Long orderId;

    @JsonPropertyDescription("优惠券id")
    private Long couponId;

    public static PrePlaceOrder of(OrderConfirmVO orderConfirmVO) {
        // 订单总金额
        double totalAmount = Optional.ofNullable(orderConfirmVO.getTotalAmount())
                .map(num -> num.doubleValue() / 100d)
                .map(num -> NumberUtil.round(num, 2).doubleValue())
                .orElse(0.0d);

        //最大优惠金额
        double discountAmount = Optional.ofNullable(CollUtil.getFirst(orderConfirmVO.getDiscounts()))
                .map(CouponDiscountDTO::getDiscountAmount)
                .map(num -> num.doubleValue() / 100d)
                .map(num -> NumberUtil.round(num, 2).doubleValue())
                .orElse(0.0d);

        //优惠券名称
        String couponName = Optional.ofNullable(CollUtil.getFirst(orderConfirmVO.getDiscounts()))
                .map(couponDiscountDTO -> {
                    List<String> rules = couponDiscountDTO.getRules();
                    int size = CollUtil.size(rules);
                    return size >= 2
                            ? StrUtil.format("叠加{}券：【优惠{}元】", size, discountAmount)
                            : StrUtil.format("单券：【{}】", CollUtil.getFirst(rules));
                })
                .orElse("");

        // 实付金额，保留小数点后两位
        double payAmount = NumberUtil.round(totalAmount - discountAmount, 2).doubleValue();

        // 课程id列表
        List<Long> courseIds = CollStreamUtil.toList(orderConfirmVO.getCourses(), OrderCourseDTO::getId);

        // 优惠券id
        Long couponId = Optional.ofNullable(CollUtil.getFirst(orderConfirmVO.getDiscounts()))
                .map(CouponDiscountDTO::getIds)
                .map(CollUtil::getFirst)
                .orElse(null);

        return PrePlaceOrder.builder()
                .count(CollUtil.size(orderConfirmVO.getCourses()))
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .couponName(couponName)
                .payAmount(payAmount)
                .courseIds(courseIds)
                .orderId(orderConfirmVO.getOrderId())
                .couponId(couponId)
                .build();
    }
}