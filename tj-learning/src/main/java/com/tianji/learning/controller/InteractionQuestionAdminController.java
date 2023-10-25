package com.tianji.learning.controller;


import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.query.QuestionAdminPageQuery;
import com.tianji.learning.domain.query.QuestionPageQuery;
import com.tianji.learning.domain.vo.QuestionAdminVO;
import com.tianji.learning.domain.vo.QuestionVO;
import com.tianji.learning.service.IInteractionQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 互动提问的问题表 前端控制器
 * </p>
 *
 * @author fsq
 * @since 2023-10-23
 */
@Api(tags = "互动问题相关接口-管理端")
@RestController
@RequestMapping("/admin/questions")
@RequiredArgsConstructor
public class InteractionQuestionAdminController {

    private final IInteractionQuestionService questionService;


    @ApiOperation("分页查询互动问题-管理端")
    @GetMapping("page")
    public PageDTO<QuestionAdminVO> queryQuestionAdminVOPage(QuestionAdminPageQuery query){
        return questionService.queryQuestionAdminVOPage(query);
    }

    @ApiOperation("管理端隐藏或显示问题")
    @PutMapping("/{id}/hidden/{hidden}")
    public void hiddenQuestion(@PathVariable("id") Long id ,@PathVariable("hidden") boolean hidden){
        questionService.hiddenQuestion(id,hidden);
    }

    @ApiOperation("管理端根据id查询问题详情")
    @GetMapping("/{id}")
    public QuestionAdminVO queryQuestionAdminById(@PathVariable("id") Long id ){
        return questionService.queryQuestionAdminById(id);
    }

 }
