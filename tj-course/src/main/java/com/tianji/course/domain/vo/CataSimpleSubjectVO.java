package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 目录简单题目信息
 */
@Data
@Schema(description = "目录简单题目信息")
@AllArgsConstructor
@NoArgsConstructor
public class CataSimpleSubjectVO {
    @Schema(description = "小节或练习id", example = "1")
    private Long cataId;

    @Schema(description = "题目id")
    private List<SubjectInfo> subjects;

    @Data
    @Schema(description = "题目信息")
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubjectInfo {
        @Schema(description = "题目id", example = "101")
        private Long id;

        @Schema(description = "题目名称", example = "Java题")
        private String name;
    }
}
