package com.tianji.api.dto.trade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程加入购物车")
public class CartsAddDTO {
    @Schema(description = "要加入购物车的课程id")
    @NotNull(message = "课程id不能为空")
    private Long courseId;
}
