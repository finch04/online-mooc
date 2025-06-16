package com.tianji.api.dto.promotion;

import com.tianji.common.utils.DateUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "优惠券详细数据")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CouponDetailSimpleVO {
    @ApiModelProperty("优惠券id，新增不需要添加，更新必填")
    private Long id;

    @ApiModelProperty("优惠券名称")
    private String name;
}
