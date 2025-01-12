package com.tianji.promotion.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户优惠券查询参数")
public class UserCouponQuery extends PageQuery {
    @NotNull
    @Schema(description = "优惠券状态，1：未使用，2：已使用，3：已过期")
    private Integer status;
}