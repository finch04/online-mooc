package com.tianji.aigc.tools;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.config.ToolResultHolder;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.tools.result.CourseInfo;
import com.tianji.api.client.course.CourseClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CourseTools {

    private final CourseClient courseClient;

    @Tool(description = Constant.Tools.QUERY_COURSE_BY_ID)
    public CourseInfo queryCourseById(@ToolParam(description = Constant.ToolParams.COURSE_ID) Long courseId,
                                      ToolContext toolContext) {

        var courseInfo = Optional
                .ofNullable(courseId)
                // 根据课程ID获取课程基础信息，如果ID为空，则不执行此步骤
                .flatMap(id -> Optional.ofNullable(this.courseClient.baseInfo(id, true)))
                // 将获取到的课程信息构建为响应对象
                .map(CourseInfo::of)
                // 如果上述任何步骤中没有获取到数据，则最终返回null
                .orElse(null);
        if (courseInfo == null) {
            return null;
        }

        var requestId = Convert.toStr(toolContext.getContext().get(Constant.REQUEST_ID));
        var field = StrUtil.format("{}_{}", StrUtil.lowerFirst(CourseInfo.class.getSimpleName()), courseInfo.getId());
        ToolResultHolder.put(requestId, field, courseInfo);

        return courseInfo;
    }

}
