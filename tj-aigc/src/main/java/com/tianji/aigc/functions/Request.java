package com.tianji.aigc.functions;


import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.tianji.aigc.constants.Constant;

public class Request {

    @JsonClassDescription(Constant.Requests.QUERY_COURSE_BY_ID)
    public record Course(
            @JsonProperty(value = "id")
            @JsonPropertyDescription("课程id")
            Long id) {
    }

    @JsonClassDescription(Constant.Requests.PRE_PLACE_ORDER)
    public record PrePlaceOrder(

            @JsonProperty(value = "userId")
            @JsonPropertyDescription("当前用户id")
            Long userId,

            @JsonProperty(value = "ids")
            @JsonPropertyDescription("课程id")
            Long[] ids
    ) {
    }

    @JsonClassDescription(Constant.Requests.ADD_COURSE_TO_CART)
    public record AddCourseToCart(
            @JsonProperty(value = "id")
            @JsonPropertyDescription("课程id")
            Long id,

            @JsonProperty(value = "userId")
            @JsonPropertyDescription("当前用户id")
            Long userId
    ) {
    }

}
