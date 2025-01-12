package com.tianji.search.controller;

import com.tianji.search.domain.vo.CourseVO;
import com.tianji.search.service.ISearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "课程推荐相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("recommend")
public class RecommendController {

    private final ISearchService searchService;

    @Operation(summary = "精品好课接口")
    @GetMapping("/best")
    public List<CourseVO> queryBestTopN(){
        return searchService.queryBestTopN();
    }

    @Operation(summary = "新课推荐接口")
    @GetMapping("/new")
    public List<CourseVO> queryNewTopN(){
        return searchService.queryNewTopN();
    }

    @Operation(summary = "精品公开课接口")
    @GetMapping("/free")
    public List<CourseVO> queryFreeTopN(){
        return searchService.queryFreeTopN();
    }

}
