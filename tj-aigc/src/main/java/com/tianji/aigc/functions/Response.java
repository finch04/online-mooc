package com.tianji.aigc.functions;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.tianji.api.dto.course.CourseBaseInfoDTO;
import com.tianji.api.dto.promotion.CouponDiscountDTO;
import com.tianji.api.dto.trade.OrderConfirmVO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Optional;

public class Response {

    public record CourseInfo(
            @JsonPropertyDescription("课程id")
            Long id,
            @JsonPropertyDescription("课程名称")
            String name,
            @JsonPropertyDescription("课程价格，单位为元，货币为人民币")
            double price,
            @JsonPropertyDescription("课程学习有效期，单位：月")
            Integer validDuration,
            @JsonPropertyDescription("适用人群，例如：初学者")
            String usePeople,
            @JsonPropertyDescription("课程详细介绍")
            String detail,
            @JsonPropertyDescription("课程的url，相对路径")
            String url
    ) {
        public static CourseInfo of(CourseBaseInfoDTO courseBaseInfoDTO) {
            return new CourseInfo(courseBaseInfoDTO.getId(),
                    courseBaseInfoDTO.getName(),
                    Optional.ofNullable(courseBaseInfoDTO.getPrice())
                            .map(num -> num.doubleValue() / 100d)
                            .map(num -> NumberUtil.round(num, 2).doubleValue())
                            .orElse(0.0d),
                    courseBaseInfoDTO.getValidDuration(),
                    courseBaseInfoDTO.getUsePeople(),
                    courseBaseInfoDTO.getDetail(),
                    "/#/details/index?id=" + courseBaseInfoDTO.getId()
            );
        }
    }

    public record PrePlaceOrderResult(
            @JsonPropertyDescription("课程数量")
            int count,

            @JsonPropertyDescription("订单总金额")
            double totalAmount,

            @JsonPropertyDescription("最大优惠金额")
            double discountAmount,

            @JsonPropertyDescription("优惠券名称")
            String couponName,

            @JsonPropertyDescription("实付金额")
            double payAmount,

            @JsonPropertyDescription("课程的url列表，相对路径")
            List<String> urls
    ) {
        public static PrePlaceOrderResult of(OrderConfirmVO orderConfirmVO) {
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
                                : StrUtil.format("单券：【】", CollUtil.getFirst(rules));
                    })
                    .orElse("");

            // 实付金额
            double payAmount = totalAmount - discountAmount;

            // 课程的url列表，相对路径
            List<String> urls = CollStreamUtil
                    .toList(orderConfirmVO.getCourses(), orderCourseDTO -> "/#/details/index?id=" + orderCourseDTO.getId());

            return new PrePlaceOrderResult(CollUtil.size(orderConfirmVO.getCourses()),
                    totalAmount,
                    discountAmount,
                    couponName,
                    payAmount,
                    urls
            );
        }
    }

    public record AddCourseToCartResult(
            @JsonPropertyDescription("是否成功，ok表示成功，error表示失败")
            String result
    ) {
        public static AddCourseToCartResult ok() {
            return new AddCourseToCartResult("ok");
        }

        public static AddCourseToCartResult error() {
            return new AddCourseToCartResult("error");
        }
    }

}
