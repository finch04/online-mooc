package com.tianji.course.domain.dto;

import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.validate.Checker;
import com.tianji.course.constants.CourseConstants;
import com.tianji.course.constants.CourseErrorInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 章节
 */
@Data
@Schema(description = "章节")
public class CataSaveDTO implements Checker {
    @Schema(description = "章、节、练习id")
    private Long id;

    @Schema(description = "目录类型1：章，2：节，3：测试", example = "1")
    @NotNull(message = "目录类型不能为空")
    private Integer type;

    @Schema(description = "章节练习名称", example = "第一章")
    private String name;

    @Schema(description = "章排序，章一定要传，小节和练习不需要传", example = "1")
    private Integer index;

    @Schema(description = "当前章的小节或练习")
    @Size(min = 1, message = "不能出现空章")
    private List<CataSaveDTO> sections;

    @Override
    public void check() {
        // 名称为空校验
        if (type == CourseConstants.CataType.CHAPTER && StringUtils.isEmpty(name)) {
            throw new BadRequestException(CourseErrorInfo.Msg.COURSE_CATAS_SAVE_NAME_NULL);
        } else if (StringUtils.isEmpty(name)) {
            throw new BadRequestException(CourseErrorInfo.Msg.COURSE_CATAS_SAVE_NAME_NULL2);
        }
        // 名称长度问题
        if (type == CourseConstants.CataType.CHAPTER && name.length() > 30) {
            throw new BadRequestException(CourseErrorInfo.Msg.COURSE_CATAS_SAVE_NAME_SIZE);
        } else if (name.length() > 30) {
            throw new BadRequestException(CourseErrorInfo.Msg.COURSE_CATAS_SAVE_NAME_SIZE2);
        }
        if (CollUtils.isEmpty(sections)) {
            throw new BadRequestException("不能出现空章");
        }
    }
}
