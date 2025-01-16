package com.tianji.aigc.functions;

import com.tianji.api.client.course.CourseClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class CourseFunction implements Function<Request.Course, Response.CourseInfo> {

    private final CourseClient courseClient;

    /**
     * 根据课程请求获取课程信息
     *
     * 此方法通过课程ID请求外部服务以获取课程信息如果课程ID为空，则返回null
     * 否则，它会尝试获取课程的基础信息，并构建响应对象如果课程信息不可用，则返回null
     *
     * @param course 课程请求，包含课程ID
     * @return 返回构建的课程信息响应对象，如果没有可用的课程信息，则返回null
     */
    @Override
    public Response.CourseInfo apply(Request.Course course) {
        // 检查课程ID是否为空，如果为空，则直接返回null
        return Optional
                .ofNullable(course.id())
                // 根据课程ID获取课程基础信息，如果ID为空，则不执行此步骤
                .flatMap(id -> Optional.ofNullable(this.courseClient.baseInfo(id, true)))
                // 将获取到的课程信息构建为响应对象
                .map(Response.CourseInfo::of)
                // 如果上述任何步骤中没有获取到数据，则最终返回null
                .orElse(null);
    }
}
