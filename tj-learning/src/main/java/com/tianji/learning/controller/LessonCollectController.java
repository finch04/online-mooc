package com.tianji.learning.controller;


import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.dto.CollectFormDTO;
import com.tianji.learning.domain.dto.LearningPlanDTO;
import com.tianji.learning.domain.query.LessonCollectQuery;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;
import com.tianji.learning.domain.vo.LessonCollectVO;
import com.tianji.learning.service.ILearningLessonService;
import com.tianji.learning.service.ILessonCollectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 学生课程表 前端控制器
 * </p>
 *
 * @author fsq
 * @since 2023-10-22
 */

@Api(tags = "我的课程相关接口")
@RestController
@RequestMapping("/collect")
@RequiredArgsConstructor
public class LessonCollectController {

    final ILessonCollectService lessonCollectService;

    @ApiOperation("分页查询我的收藏")
    @GetMapping("page")
    public PageDTO<LessonCollectVO> queryMyCollects(LessonCollectQuery query){
        return lessonCollectService.queryMyCollects(query);
    }

    @ApiOperation("用户收藏/取消收藏课程")
    @PostMapping()
    public void addCollect(@RequestBody CollectFormDTO dto){
        lessonCollectService.addCollect(dto);
    }

    @ApiOperation("该课程该用户是否收藏")
    @GetMapping("/{lessonId}")
    public Boolean isCollected(@PathVariable Long lessonId){
        return lessonCollectService.isCollected(lessonId);
    }

}
