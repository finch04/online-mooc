package com.tianji.aigc.functions;

import cn.hutool.core.util.NumberUtil;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.tianji.api.dto.course.CourseBaseInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public class Response {

    public record CourseInfo(
            @JsonPropertyDescription("课程id")
            Long id,
            @JsonPropertyDescription("课程名称")
            String name,
            @JsonPropertyDescription("课程价格，单位为元，货币为人民币")
            Integer price,
            @JsonPropertyDescription("课程学习有效期，单位：月")
            Integer validDuration,
            @JsonPropertyDescription("适用人群，例如：初学者")
            String usePeople,
            @JsonPropertyDescription("课程详细介绍")
            String detail,
            @JsonPropertyDescription("课程的url，相对路径")
            String url
    ) {
        public static CourseInfo of(CourseBaseInfoDTO courseBaseInfoDTO) {
            return new CourseInfo(courseBaseInfoDTO.getId(),
                    courseBaseInfoDTO.getName(),
                    Optional.ofNullable(courseBaseInfoDTO.getPrice())
                            .map(num -> num / 100).orElse(0),
                    courseBaseInfoDTO.getStatus(),
                    courseBaseInfoDTO.getUsePeople(),
                    courseBaseInfoDTO.getDetail(),
                    "/#/details/index?id=" + courseBaseInfoDTO.getId()
            );
        }
    }

}
