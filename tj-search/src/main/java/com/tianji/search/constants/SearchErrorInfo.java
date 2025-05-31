package com.tianji.search.constants;

public interface SearchErrorInfo {
    String UPDATE_COURSE_STATUS_ERROR = "更新课程状态异常";
    String SAVE_COURSE_ERROR = "新增课程索引异常";
    String QUERY_COURSE_ERROR = "查询课程异常";
    String TEACHER_NOT_EXISTS = "教师信息不存在";
    String STAFF_NOT_EXISTS = "员工信息不存在";

    // 订单相关异常
    String SAVE_ORDER_ERROR = "保存订单异常";
    String QUERY_ORDER_ERROR = "查询订单异常";
    String UPDATE_ORDER_STATUS_ERROR = "更新订单状态异常";
    String ORDER_NOT_EXISTS = "订单不存在";

    //订单详情相关异常
    String SAVE_ORDER_DETAIL_ERROR = "保存订单详情异常";
    String QUERY_ORDER_DETAIL_ERROR = "查询订单详情异常";
    String UPDATE_ORDER_DETAIL_STATUS_ERROR = "更新订单详情状态异常";
    String ORDER_DETAIL_NOT_EXISTS = "订单详情不存在";
}
