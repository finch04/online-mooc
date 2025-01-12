package com.tianji.data.model.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class BoardDataSetDTO {
    @NotNull(message = "版本")
    private Integer version;
    @NotNull(message = "数据类型不能为空")
    @Min(value = 1, message = "数据类型1-9")
    @Max(value = 9, message = "数据类型1-9")
    private Integer type;
    @NotNull(message = "设置数据不能为空")
    @Size(min = 15, max = 15, message = "需要设置15天的数据")
    private List<Double> data;
}
