package com.tianji.data.model.po;

import lombok.Data;

import java.util.Map;

@Data
public class UserProfile {
    private String userId;
    private String userName;
    private String sex;
    private String province;
    // 课程访问量统计，key 为课程 ID，value 为访问量
    private Map<String, Long> courseVisitCounts;
}