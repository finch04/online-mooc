package com.tianji.course.mq;/**
 * @author fsq
 * @date 2025/5/23 8:33
 */

import com.tianji.api.dto.leanring.EvaluationScoreDTO;
import com.tianji.common.constants.MqConstants;
import com.tianji.course.domain.po.Course;
import com.tianji.course.service.ICourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/5/23 8:33
 * @Version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseAvgScoreUpdateListener {

    private  final ICourseService courseService;


    /**
     * 课程定时更新评分
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "course.comment.queue",durable = "true"),
            exchange = @Exchange(value = MqConstants.Exchange.COURSE_EXCHANGE,type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.COURSE_COMMENT_KEY
    ))
    public void listenSignInListener(List<EvaluationScoreDTO> dtoList){
        log.debug("消费到课程更新的评分 消费到消息：{}",dtoList);
        List<Course> courseList= new ArrayList<>();
        for (EvaluationScoreDTO evaluationScoreDTO : dtoList) {
            Course course = courseService.getById(evaluationScoreDTO.getCourseId());
            course.setScore((int)(evaluationScoreDTO.getOverallRating()*10));
            courseList.add(course);
        }
        courseService.updateBatchById(courseList);
    }


}
