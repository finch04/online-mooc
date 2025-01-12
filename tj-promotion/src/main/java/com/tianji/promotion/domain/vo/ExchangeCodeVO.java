package com.tianji.promotion.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "兑换码实体")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeCodeVO {
    @Schema(description = "兑换码id")
    private Integer id;
    @Schema(description = "兑换码")
    private String code;
}
