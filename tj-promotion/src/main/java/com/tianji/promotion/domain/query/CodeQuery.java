package com.tianji.promotion.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "兑换码查询参数")
public class CodeQuery extends PageQuery {
    @Schema(description = "兑换码对应的优惠券id")
    @NotNull
    private Long couponId;
    @Schema(description = "兑换码状态，1：未兑换，2：已兑换")
    @NotNull
    private Integer status;
}
