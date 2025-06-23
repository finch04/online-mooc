package com.tianji.data.model.vo;

import lombok.Data;

import java.util.Map;

@Data
public class UserProfileVO {
    private Long userId;
    private String userName;
    private String sex;
    private String province;
    private String icon;
    // 课程访问量统计，key 为课程 ID，value 为访问量
    private Map<Long, Long> courseVisitCounts;
}