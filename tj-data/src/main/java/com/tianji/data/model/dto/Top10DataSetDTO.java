package com.tianji.data.model.dto;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Validated
public class Top10DataSetDTO {
    private Integer version = 0;
    @Size(min = 10, message = "数据最少设置10个")
    private List<Top10DataSetUnitDTO> data;

    @Data
    @Validated
    public static class Top10DataSetUnitDTO {
        @NotNull(message = "分类名称不能为空")
        private String category;
        @NotNull(message = "课程名称不能为空")
        private String name;
        @NotNull(message = "新增学员人数不能为空")
        private Integer newStuNum;
        @NotNull(message = "订单金额不能为空")
        private Double orderAmount;
    }
}
