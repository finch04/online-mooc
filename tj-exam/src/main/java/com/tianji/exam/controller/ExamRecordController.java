package com.tianji.exam.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.exam.domain.dto.ExamCommitDTO;
import com.tianji.exam.domain.dto.ExamFormDTO;
import com.tianji.exam.domain.vo.ExamQuestionVO;
import com.tianji.exam.domain.vo.ExamRecordDetailVO;
import com.tianji.exam.domain.vo.ExamRecordVO;
import com.tianji.exam.service.IExamRecordService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 考试记录表 控制器
 * </p>
 *
 * @author 虎哥
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/exams")
public class ExamRecordController {

    private final IExamRecordService examService;


    @GetMapping("/page")
    @ApiOperation("分页查询我的考试记录")
    public PageDTO<ExamRecordVO> queryMyExamRecords(PageQuery query){
        return examService.queryMyExamRecordsPage(query);
    }

    @ApiOperation("查询我的考试记录详情")
    @GetMapping("/{examId}")
    public List<ExamRecordDetailVO> queryDetailsByExamId(
            @ApiParam(value = "考试记录id", example = "1") @PathVariable("examId") Long examId
    ){
        return examService.queryDetailsByExamId(examId);
    }

    @ApiOperation("新增考试记录，考试或测试开始时需要保存基本信息，返回记录id")
    @PostMapping
    public ExamQuestionVO saveExamRecord(@RequestBody ExamFormDTO examFormDTO){
        return examService.saveExamRecord(examFormDTO);
    }

    @ApiOperation("提交考试答案，考试或测试提交时需要保存答案信息")
    @PostMapping("/details")
    public void saveExamRecordDetails(@RequestBody ExamCommitDTO examCommitDTO){
        examService.saveExamRecordDetails(examCommitDTO);
    }
}
