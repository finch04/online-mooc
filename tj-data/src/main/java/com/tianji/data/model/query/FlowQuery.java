package com.tianji.data.model.query;/**
 * @author fsq
 * @date 2025/6/21 10:35
 */

import com.tianji.common.utils.DateUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @Author: fsq
 * @Date: 2025/6/21 10:35
 * @Version: 1.0
 */

@Data
@ApiModel(description = "流量统计查询条件")
public class FlowQuery {
    @ApiModelProperty(value = "更新时间区间的开始时间", example = "2022-7-18 19:52:36")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime beginTime;
    @ApiModelProperty(value = "更新时间区间的结束时间", example = "2022-7-18 19:52:36")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime endTime;
}