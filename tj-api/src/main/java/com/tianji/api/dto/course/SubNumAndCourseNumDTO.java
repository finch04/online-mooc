package com.tianji.api.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 老师负责的课程数和出题数目的集合
 * @ClassName SubNumAndCourseNumDTO
 * @author wusongsong
 * @since 2022/7/18 15:12
 * @version 1.0.0
 **/
@Data
@AllArgsConstructor
@Schema(description = "老师id和老师对应的课程数，出题数")
public class SubNumAndCourseNumDTO {
    @Schema(description = "老师id")
    private Long teacherId;
    @Schema(description = "老师负责的课程数")
    private Integer courseNum;
    @Schema(description = "老师出题数")
    private Integer subjectNum;
}
