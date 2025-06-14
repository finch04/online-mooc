package com.tianji.course.handler;/**
 * @author fsq
 * @date 2025/5/25 10:03
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.api.dto.data.CourseDataVO;
import com.tianji.api.dto.data.OrderDataVO;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.course.domain.po.Course;
import com.tianji.course.service.ICourseService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/5/25 10:03
 * @Version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseDataJobHandler {
    private final StringRedisTemplate redisTemplate;
    private final ICourseService courseService;
    private final RabbitMqHelper mqHelper;

    @XxlJob("courseDataJobHandler")
    public void handleCourseData() {
        // 查询所有未删除的课程
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getDeleted, 0); // 0表示未删除

        List<Course> list = courseService.list(queryWrapper);
        CourseDataVO courseDataVO = new CourseDataVO();

        // 初始化统计指标
        int courseNum = 0;          // 课程总数量
        int upCourse = 0;           // 上架课程
        int downCourse = 0;         // 下架课程
        int waitUpCourse = 0;       // 待上架课程
        int endCourse = 0;          // 完结课程

        // 遍历课程列表进行统计
        for (Course course : list) {
            courseNum++; // 累计课程总数

            Integer status = course.getStatus();
            if (status != null) {
                switch (status) {
                    case 1:  // 待上架
                        waitUpCourse++;
                        break;
                    case 2:  // 已上架
                        upCourse++;
                        break;
                    case 3:  // 下架
                        downCourse++;
                        break;
                    case 4:  // 已完结
                        endCourse++;
                        break;
                }
            }
        }

        // 设置统计结果
        courseDataVO.setCourseNum(courseNum);
        courseDataVO.setUpCourse(upCourse);
        courseDataVO.setDownCourse(downCourse);
        courseDataVO.setWaitUpCourse(waitUpCourse);
        courseDataVO.setEndCourse(endCourse);

        // 发送到数据微服务
        mqHelper.send(MqConstants.Exchange.DATA_EXCHANGE,
                MqConstants.Key.COURSE_DATA_KEY, courseDataVO);

        log.info("课程数据统计任务完成，共处理 {} 条数据", list.size());
        log.info("统计结果：课程总数={}，上架课程={}，下架课程={}，待上架课程={}，完结课程={}",
                courseNum, upCourse, downCourse, waitUpCourse, endCourse);
    }

}
