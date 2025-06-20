package com.tianji.data.constants;


/**
 * @ClassName LogBusinessEnum.java
 * @Description 数据埋点枚举
 */
public enum LogBusinessEnum {

    PAGE_FAIL(29001, "查询数据字典列表失败"),
    SAVE_FAIL(29002,"日志保存失败");

    private Integer code;
    private String msg;

    LogBusinessEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
