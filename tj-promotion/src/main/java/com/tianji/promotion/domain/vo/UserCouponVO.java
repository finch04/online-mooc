package com.tianji.promotion.domain.vo;

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
@Schema(description = "用户优惠券")
public class UserCouponVO {

    @Schema(description = "优惠券id")
    private Long id;

    @Schema(description = "优惠券名称")
    private String name;

    @Schema(description = "是否限定范围, false: 无限制， true：限定范围")
    private Boolean specify;

    @Schema(description = "折扣规则")
    private String rule;

    @Schema(description = "优惠金额")
    private Integer discountValue;

    @Schema(description = "优惠类型，type：1：每满减，2：折扣 3：无门槛，4：普通满减")
    private Integer discountType;

    @Schema(description = "优惠券有效期截止时间")
    private LocalDateTime termEndTime;
}
