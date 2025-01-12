package com.tianji.message.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.dto.NoticeTaskDTO;
import com.tianji.message.domain.dto.NoticeTaskFormDTO;
import com.tianji.message.domain.query.NoticeTaskPageQuery;
import com.tianji.message.service.INoticeTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 系统通告的任务表，可以延期或定期发送通告 前端控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/notice-tasks")
@Tag(name = "短信任务管理接口")
public class NoticeTaskController {

    private final INoticeTaskService noticeTaskService;

    @PostMapping
    @Operation(summary = "新增通知任务")
    public Long saveNoticeTask(@RequestBody NoticeTaskFormDTO noticeTaskFormDTO) {
        return noticeTaskService.saveNoticeTask(noticeTaskFormDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新通知任务")
    public void updateNoticeTask(
            @RequestBody NoticeTaskFormDTO noticeTaskFormDTO,
            @Parameter(description = "任务id", example = "1") @PathVariable("id") Long id) {
        noticeTaskFormDTO.setId(id);
        noticeTaskService.updateNoticeTask(noticeTaskFormDTO);
    }

    @GetMapping
    @Operation(summary = "分页查询通知任务")
    public PageDTO<NoticeTaskDTO> queryNoticeTasks(NoticeTaskPageQuery pageQuery) {
        return noticeTaskService.queryNoticeTasks(pageQuery);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询任务")
    public NoticeTaskDTO queryNoticeTask(@Parameter(description = "任务id", example = "1") @PathVariable("id") Long id) {
        return noticeTaskService.queryNoticeTask(id);
    }
}
