package com.tianji.data.model.po;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LogAnalysisResult {
    // 用户画像列表
    private List<UserProfile> userProfiles;
    // 课程画像列表
    private List<CourseProfile> courseProfiles;
}