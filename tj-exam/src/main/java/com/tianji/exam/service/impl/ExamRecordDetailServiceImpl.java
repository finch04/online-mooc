package com.tianji.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.exam.domain.dto.ExamDetailCommentDTO;
import com.tianji.exam.domain.po.ExamRecordDetail;
import com.tianji.exam.mapper.ExamRecordDetailMapper;
import com.tianji.exam.service.IExamRecordDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 考试记录明细，学生答案 服务实现类
 * </p>
 *
 * @author 虎哥
 */
@Service
public class ExamRecordDetailServiceImpl extends ServiceImpl<ExamRecordDetailMapper, ExamRecordDetail> implements IExamRecordDetailService {

    @Override
    public void removeByExamId(Long examId) {
        remove(new QueryWrapper<ExamRecordDetail>().lambda().eq(ExamRecordDetail::getExamId, examId));
    }

    @Override
    public void addComment(List<ExamDetailCommentDTO> dtos) {
        for (ExamDetailCommentDTO dto : dtos) {
            this.lambdaUpdate().
                    set(ExamRecordDetail::getComment, dto.getComment())
                    .eq(ExamRecordDetail::getId, dto.getId())
                    .update();
        }
    }
}
