package com.tianji.search.service;

import com.tianji.api.dto.course.CourseSearchDTO;

import java.util.List;

public interface ICourseService {

    void handleCourseDelete(Long courseId);

    void handleCourseUp(Long courseId);

    void updateCourseSold(List<Long> courseId, int amount);

    void handleCourseDeletes(List<Long> courseIds);

    //新增提词库索引文档
    void saveSuggestDoc(CourseSearchDTO dto);
}
