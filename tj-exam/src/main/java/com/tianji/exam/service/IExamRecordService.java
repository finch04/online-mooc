package com.tianji.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.exam.domain.dto.ExamCommitDTO;
import com.tianji.exam.domain.dto.ExamFormDTO;
import com.tianji.exam.domain.po.ExamRecord;
import com.tianji.exam.domain.query.ExamPageQuery;
import com.tianji.exam.domain.vo.ExamQuestionVO;
import com.tianji.exam.domain.vo.ExamRecordAdminVO;
import com.tianji.exam.domain.vo.ExamRecordDetailVO;
import com.tianji.exam.domain.vo.ExamRecordVO;

import java.util.List;

/**
 * <p>
 * 考试记录表 服务类
 * </p>
 *
 * @author 虎哥
 */
public interface IExamRecordService extends IService<ExamRecord> {

    PageDTO<ExamRecordVO> queryMyExamRecordsPage(PageQuery query);

    ExamQuestionVO saveExamRecord(ExamFormDTO examFormDTO);

    void saveExamRecordDetails(ExamCommitDTO examCommitDTO);

    List<ExamRecordDetailVO> queryDetailsByExamId(Long examId);

    PageDTO<ExamRecordAdminVO> queryAdminExamRecordsPage(ExamPageQuery query);

    List<ExamRecordDetailVO> queryAdminDetailsByExamId(Long examId);

}
