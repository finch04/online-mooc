package com.tianji.api.dto.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName TodayDataVO
 * @Author wusongsong
 * @Date 2022/10/13 9:23
 * @Version
 **/
@Data
public class CourseDataVO {
    @ApiModelProperty("课程总数量")
    private Integer courseNum;
    @ApiModelProperty("上架课程")
    private Integer upCourse;
    @ApiModelProperty("下架课程")
    private Integer downCourse;
    @ApiModelProperty("待上架课程")
    private Integer waitUpCourse;
    @ApiModelProperty("完结课程")
    private Integer endCourse;
}
