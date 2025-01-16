package com.tianji.search.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.search.domain.query.CoursePageQuery;
import com.tianji.search.domain.vo.CourseVO;
import com.tianji.search.service.ICourseService;
import com.tianji.search.service.ISearchService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("courses")
@Tag(name = "课程搜索接口")
public class CourseController {

    private final ISearchService searchService;
    private final ICourseService courseService;

    @Operation(summary = "用户端课程搜索接口")
    @GetMapping("/portal")
    public PageDTO<CourseVO> queryCoursesForPortal(CoursePageQuery query){
        return searchService.queryCoursesForPortal(query);
    }

    // @Hidden
    @GetMapping("/name")
    public List<Long> queryCoursesIdByName(@RequestParam("keyword") String keyword){
        return searchService.queryCoursesIdByName(keyword);
    }

    @Operation(summary = "处理指定课程上架失败的问题")
    @PostMapping("/up")
    public void handleCoursesUp(
            @io.swagger.v3.oas.annotations.Parameter(description = "课程id集合") @RequestParam("courseIds") List<Long> courseIds) {
        for (Long courseId : courseIds) {
            courseService.handleCourseUp(courseId);
        }
    }

    @Operation(summary = "处理指定课程下架失败的问题")
    @PostMapping("/down")
    public void handleCoursesDown(
            @io.swagger.v3.oas.annotations.Parameter(description = "课程id集合") @RequestParam("courseIds") List<Long> courseIds) {
        for (Long courseId : courseIds) {
            // TODO 缺少方法，暂时不处理
            // courseService.handleCourseDown(courseId);
        }
    }
}
