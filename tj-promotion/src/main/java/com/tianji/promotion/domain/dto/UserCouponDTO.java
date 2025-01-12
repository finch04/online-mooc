package com.tianji.promotion.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户优惠券DTO")
public class UserCouponDTO {
    @Schema(description = "用户id")
    private Long userId;
    @Schema(description = "优惠券id")
    private Long couponId;
    @Schema(description = "兑换码序列号（id）")
    private Integer serialNum;
}