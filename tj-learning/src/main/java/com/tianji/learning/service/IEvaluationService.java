package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.api.dto.leanring.EvaluationScoreDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.EvaluationDTO;
import com.tianji.learning.domain.po.Evaluation;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.query.EvaluationQuery;
import com.tianji.learning.domain.vo.EvaluationDetailVO;
import com.tianji.learning.domain.vo.EvaluationVO;

import java.util.List;

/**
 * @author fsq
 * @date 2025/5/22 11:39
 */
public interface IEvaluationService extends IService<Evaluation> {

    PageDTO<EvaluationVO> queryEvaluationPage(EvaluationQuery query);

    void saveEvaluation(EvaluationDTO dto);

    EvaluationDTO queryEvaluationById(Long id);

    Boolean updateEvaluation(EvaluationDTO dto);

    Boolean deleteEvaluation(Long id);

    Boolean isEvaluated(Long courseId);

    EvaluationDetailVO queryEvaluationDetailById(Long id);

    void  getAllCourseAvgScore();
}
