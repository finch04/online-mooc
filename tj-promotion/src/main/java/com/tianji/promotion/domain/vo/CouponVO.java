package com.tianji.promotion.domain.vo;

import com.tianji.promotion.enums.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户端优惠券信息")
public class CouponVO {
    @Schema(description = "优惠券id，新增不需要添加，更新必填")
    private Long id;
    @Schema(description = "优惠券名称")
    private String name;
    @Schema(description = "是否限定使用范围")
    private Boolean specific;

    @Schema(description = "优惠券类型，1：每满减，2：折扣，3：无门槛，4：普通满减")
    private DiscountType discountType;
    @Schema(description = "折扣门槛，0代表无门槛")
    private Integer thresholdAmount;
    @Schema(description = "折扣值，满减填抵扣金额；打折填折扣值：80标示打8折")
    private Integer discountValue;
    @Schema(description = "最大优惠金额")
    private Integer maxDiscountAmount;

    @Schema(description = "有效天数")
    private Integer termDays;
    @Schema(description = "使用有效期结束时间")
    private LocalDateTime termEndTime;

    @Schema(description = "是否可以领取")
    private Boolean available;

    @Schema(description = "是否可以使用")
    private Boolean received;
}
