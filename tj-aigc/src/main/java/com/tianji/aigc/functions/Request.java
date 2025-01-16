package com.tianji.aigc.functions;


import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class Request {

    @JsonClassDescription("根据课程id查询课程信息")
    public record Course(
            @JsonProperty(value = "id")
            @JsonPropertyDescription("课程id")
            Long id) {
    }

}
