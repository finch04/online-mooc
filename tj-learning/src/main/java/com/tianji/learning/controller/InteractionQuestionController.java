package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.query.QuestionPageQuery;
import com.tianji.learning.domain.vo.QuestionVO;
import com.tianji.learning.service.IInteractionQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 互动提问的问题表 前端控制器
 */
@RestController
@RequestMapping("/questions")
@Tag(name = "互动问答的相关接口")
@RequiredArgsConstructor
public class InteractionQuestionController {

    private final IInteractionQuestionService questionService;

    @Operation(summary = "新增互动问题")
    @PostMapping
    public void saveQuestion(@Valid @RequestBody QuestionFormDTO questionDTO) {
       questionService.saveQuestion(questionDTO);
    }

    @Operation(summary = "修改提问")
    @PutMapping("/{id}")
    public void updateQuestion(
            @Parameter(description = "要修改的问题的id") @PathVariable("id") Long id,
            @RequestBody QuestionFormDTO questionDTO) {
        questionService.updateQuestion(id, questionDTO);
    }

    @Operation(summary = "分页查询互动问题")
    @GetMapping("page")
    public PageDTO<QuestionVO> queryQuestionPage(QuestionPageQuery query) {
        return questionService.queryQuestionPage(query);
    }

    @Operation(summary = "根据id查询互动问题")
    @GetMapping("{id}")
    public QuestionVO queryQuestionById(@PathVariable("id") Long id) {
        return questionService.queryQuestionById(id);
    }

    @Operation(summary = "根据id删除当前用户问题")
    @DeleteMapping("/{id}")
    public void deleteQuestion(
            @Parameter(description = "问题id", example = "1") @PathVariable("id") Long id) {
        questionService.deleteById(id);
    }
}
