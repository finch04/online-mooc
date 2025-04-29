package com.tianji.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.exam.domain.po.ExamRecordDetail;

/**
 * <p>
 * 考试记录明细，学生答案 服务类
 * </p>
 *
 * @author 虎哥
 */
public interface IExamRecordDetailService extends IService<ExamRecordDetail> {
    void removeByExamId(Long examId);
}
