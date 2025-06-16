package com.tianji.aigc.tools;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.config.ToolResultHolder;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.tools.result.CourseInfo;
import com.tianji.aigc.tools.result.PrePlaceOrder;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.trade.TradeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 课程工具
 */
@Component
@RequiredArgsConstructor
public class CourseTools {

    public static final String FIELD_NAME_FORMAT = "{}_{}";

    private final CourseClient courseClient;


    @Tool(description = Constant.Tools.QUERY_COURSE_BY_ID)
    public CourseInfo queryCourseById(@ToolParam(description = Constant.ToolParams.COURSE_ID) Long courseId,
                                      ToolContext toolContext) {
        return Optional.ofNullable(courseId)
                .map(id -> this.courseClient.baseInfo(id, true))
                .map(CourseInfo::of)
                .map(courseInfo -> {
                    // 将课程数据存储到数据容器中

                    // 通过工具上下文获取到请求id
                    var requestId = Convert.toStr(toolContext.getContext().get(Constant.REQUEST_ID));

                    // 设置field key
                    var field = StrUtil.format(FIELD_NAME_FORMAT, StrUtil.lowerFirst(CourseInfo.class.getSimpleName()), courseId);

                    // 存储数据
                    ToolResultHolder.put(requestId, field, courseInfo);
                    return courseInfo;
                })
                .orElse(null);
    }



}
