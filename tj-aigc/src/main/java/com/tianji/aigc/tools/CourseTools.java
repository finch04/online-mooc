package com.tianji.aigc.tools;

import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.tools.result.CourseInfo;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CourseBaseInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CourseTools extends BaseTools {

    private final CourseClient courseClient;

    private static final String FIELD_NAME_FORMAT = "{}_{}";  // 提取格式字符串常量

    @Tool(description = Constant.Tools.QUERY_COURSE_BY_ID)
    public CourseInfo queryCourseById(
            @ToolParam(description = Constant.ToolParams.COURSE_ID) Long courseId,
            ToolContext toolContext) {

        return Optional.ofNullable(courseId)
                .map(id -> CourseInfo.of(this.courseClient.baseInfo(id, true)))
                .map(courseInfo -> {
                    String field = StrUtil.format(FIELD_NAME_FORMAT,
                            StrUtil.lowerFirst(CourseInfo.class.getSimpleName()),
                            courseInfo.getId());
                    return super.saveResult(toolContext, field, courseInfo);
                })
                .orElse(null);
    }


}
