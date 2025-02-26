package com.tianji.aigc.constants;

public interface Constant {

    String USER_ID = "userId";

    interface Functions {
        String COURSE_FUNCTION = "courseFunction";
        String CART_ADD_FUNCTION = "cartAddFunction";
        String PRE_PLACE_ORDER_FUNCTION = "prePlaceOrderFunction";
    }

    interface Requests {
        String QUERY_COURSE_BY_ID = "根据课程id查询";
        String ADD_COURSE_TO_CART = "添加课程到购物车，入参为课程id";
        String PRE_PLACE_ORDER = "购买课程预下单操作，入参为多个课程id";
    }

}
