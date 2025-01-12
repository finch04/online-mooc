package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.NoteFormDTO;
import com.tianji.learning.domain.query.NotePageQuery;
import com.tianji.learning.domain.vo.NoteVO;
import com.tianji.learning.service.INoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 笔记相关接口
 */
@RestController
@RequestMapping("/notes")
@Tag(name = "笔记相关接口")
@RequiredArgsConstructor
public class NoteController {

    private final INoteService noteService;

    @Operation(summary = "新增笔记")
    @PostMapping
    public void saveNote(@Valid @RequestBody NoteFormDTO noteDTO) {
        noteService.saveNote(noteDTO);
    }

    @Operation(summary = "采集笔记")
    @PostMapping("/gathers/{id}")
    public void gatherNote(
            @Parameter(description = "笔记id", example = "1") @PathVariable("id") Long id) {
        noteService.gatherNote(id);
    }

    @Operation(summary = "取消采集笔记")
    @DeleteMapping("/gathers/{id}")
    public void removeGatherNote(
            @Parameter(description = "笔记id", example = "1") @PathVariable("id") Long id) {
        noteService.removeGatherNote(id);
    }

    @Operation(summary = "更新笔记")
    @PutMapping("/{id}")
    public void updateNote(
            @Parameter(description = "笔记id", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody NoteFormDTO noteDTO) {
        noteDTO.setId(id);
        noteService.updateNote(noteDTO);
    }

    @Operation(summary = "删除我的笔记")
    @DeleteMapping("/{id}")
    public void removeMyNote(
            @Parameter(description = "笔记id", example = "1") @PathVariable("id") Long id) {
        noteService.removeMyNote(id);
    }

    @Operation(summary = "用户端分页查询笔记")
    @GetMapping("/page")
    public PageDTO<NoteVO> queryNotePage(@Valid NotePageQuery query) {
        return noteService.queryNotePage(query);
    }
}
