package com.tianji.search.domain.query;

import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程搜索条件")
public class CoursePageQuery extends PageQuery {

    @Schema(description = "搜索关键字", example = "Redis")
    private String keyword;
    @Schema(description = "课程1级分类id", example = "1")
    private Long categoryIdLv1;
    @Schema(description = "课程2级分类id", example = "1")
    private Long categoryIdLv2;
    @Schema(description = "课程3级分类id", example = "1")
    private Long categoryIdLv3;
    @Schema(description = "售卖模式，true：免费，false：收费", example = "true")
    private Boolean free;
    @Schema(description = "课程状态，1：待上架，2：已上架，3：已下架，4：已完结", example = "1")
    private Integer status;
    @Schema(description = "课程类型，1-录播，2-直播", example = "1")
    private Integer type;
    @Schema(description = "更新时间区间的开始时间", example = "2022-7-18 19:52:36")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;
    @Schema(description = "更新时间区间的结束时间", example = "2022-7-18 19:52:36")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime endTime;
}
