package com.tianji.learning.controller;/**
 * @author fsq
 * @date 2025/5/22 11:37
 */

import com.tianji.api.dto.leanring.EvaluationScoreDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.EvaluationDTO;
import com.tianji.learning.domain.query.EvaluationQuery;
import com.tianji.learning.domain.vo.EvaluationDetailVO;
import com.tianji.learning.domain.vo.EvaluationVO;
import com.tianji.learning.service.IEvaluationService;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: fsq
 * @Date: 2025/5/22 11:37
 * @Version: 1.0
 */


@RestController
@RequiredArgsConstructor
@RequestMapping("/evaluation")
public class EvaluationController {

    private final IEvaluationService evaluationService;

    @ApiModelProperty("分页查询课程评价")
    @GetMapping("/page")
    public PageDTO<EvaluationVO> queryEvaluationPage(EvaluationQuery query){
        return evaluationService.queryEvaluationPage(query);
    }

    @ApiModelProperty("是否评价过该课程")
    @GetMapping("/evaluated/{courseId}")
    public Boolean isEvaluated(@PathVariable("courseId") Long courseId){
        return evaluationService.isEvaluated(courseId);
    }


    @ApiModelProperty("新增课程评价")
    @PostMapping
    public void saveEvaluation(@RequestBody EvaluationDTO dto){
        evaluationService.saveEvaluation(dto);
    }

    @ApiModelProperty("编辑课程评价")
    @PutMapping("/{id}")
    public Boolean updateEvaluation(@ApiParam(value = "评价id", example = "1") @PathVariable("id") Long id,
                                   @RequestBody  EvaluationDTO dto){
        dto.setId(id);
        return evaluationService.updateEvaluation(dto);
    }

//    @ApiModelProperty("查询课程评价详情")
//    @GetMapping("/detail/{id}")
//    public EvaluationDetailVO queryEvaluationDetailById(@PathVariable("id") Long id){
//        return evaluationService.queryEvaluationDetailById(id);
//    }

    @ApiModelProperty("用户编辑自己评价，查询评价详情")
    @GetMapping("/{id}")
    public EvaluationDTO queryEvaluationById(@PathVariable("id") Long id){
        return evaluationService.queryEvaluationById(id);
    }

    @ApiModelProperty("删除课程评价")
    @DeleteMapping("/{id}")
    public Boolean updateEvaluation(@ApiParam(value = "评价id", example = "1") @PathVariable("id") Long id){
        return evaluationService.deleteEvaluation(id);
    }

//    @ApiModelProperty("根据课程id得到其平均评价分")
//    @GetMapping("/score/{courseId}")
//    public EvaluationScoreDTO getAvgScoreByCourseId(@PathVariable("courseId") Long courseId){
//        return evaluationService.getAvgScoreByCourseId(courseId);
//    }


}
