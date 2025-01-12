package com.tianji.course.domain.dto;

import com.tianji.course.constants.CourseErrorInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 保存老师课程关系
 */
@Data
@Schema(description = "课程老师关系模型")
public class CourseTeacherSaveDTO {
    @Schema(description = "课程id", example = "1")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_TEACHER_SAVE_COURSE_ID_NULL)
    private Long id;

    @Schema(description = "老师id和用户端是否展示，该列表按照界面上的顺序")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_TEACHER_SAVE_TEACHERS_NULL)
    @Size(min = 1, max = 5, message = CourseErrorInfo.Msg.COURSE_TEACHER_SAVE_TEACHERS_NUM_MAX)
    private List<TeacherInfo> teachers;

    @Data
    @Schema(description = "老师id和用户端是否显示")
    public static class TeacherInfo {
        @Schema(description = "老师id", example = "101")
        @NotNull(message = CourseErrorInfo.Msg.COURSE_TEACHER_SAVE_TEACHER_ID_NULL)
        private Long id;

        @Schema(description = "用户端是否展示", example = "true")
        @NotNull(message = CourseErrorInfo.Msg.COURSE_TEACHER_SAVE_TEACHER_SHOW)
        private Boolean isShow;
    }
}
