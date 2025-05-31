package com.tianji.learning.task;/**
 * @author fsq
 * @date 2025/5/23 8:29
 */

import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.service.IEvaluationService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author: fsq
 * @Date: 2025/5/23 8:29
 * @Version: 1.0
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseEvaluationScoreUpdateHandler {

    private final IEvaluationService evaluationService;

    @XxlJob("getAllCourseAvgScoreToUpdate")
    public void getAllCourseAvgScoreToUpdate(){
        evaluationService.getAllCourseAvgScore();
    }
}
