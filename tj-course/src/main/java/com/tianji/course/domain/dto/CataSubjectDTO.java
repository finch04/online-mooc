package com.tianji.course.domain.dto;

import com.tianji.course.constants.CourseErrorInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 小节、练习和题目关系模型
 */
@Data
@Schema(description = "小节、练习和题目关系模型")
public class CataSubjectDTO {
    @Schema(description = "题目id", example = "[1, 2, 3]")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SUBJECT_SAVE_SUBJECT_IDS_NULL)
    @Size(min = 1, message = CourseErrorInfo.Msg.COURSE_SUBJECT_SAVE_SUBJECT_IDS_NULL)
    private List<Long> subjectIds;

    @Schema(description = "小节或练习id", example = "1")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SUBJECT_SAVE_CATALOGUE_ID_NULL)
    private Long cataId;
}
