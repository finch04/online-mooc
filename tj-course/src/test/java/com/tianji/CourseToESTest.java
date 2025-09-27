package com.tianji;

import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.course.domain.po.Course;
import com.tianji.course.service.ICourseDraftService;
import com.tianji.course.service.ICourseService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/9/22 15:34
 * @Version: 1.0
 */
@SpringBootTest
public class CourseToESTest {

    @Resource
    private ICourseDraftService courseDraftService;
    @Resource
    private ICourseService courseService;
    @Resource
    private RabbitMqHelper rabbitMqHelper;

    @Test
    public void test() {
        List<Course> list = courseService.lambdaQuery()
                .eq(Course::getStatus, 2)
                .eq(Course::getDeleted, 0).list();
        for (Course course : list) {
            //5.课程上架mq
            rabbitMqHelper.send(MqConstants.Exchange.COURSE_EXCHANGE,
                    MqConstants.Key.COURSE_UP_KEY,
                    course.getId());
            System.out.println("课程上架："+course.getId());
        }
    }
}
