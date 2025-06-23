package com.tianji.data.model.vo;

import com.tianji.data.model.po.UserProfile;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LogAnalysisResultVO {
    // 用户画像列表
    private List<UserProfile> userProfiles;
    // 按性别统计的课程访问量，key 为性别，value 为课程 ID 到访问量的映射
    private Map<String, Map<String, Long>> courseVisitBySex;
    // 按省份统计的课程访问量，key 为省份，value 为课程 ID 到访问量的映射
    private Map<String, Map<String, Long>> courseVisitByProvince;
}