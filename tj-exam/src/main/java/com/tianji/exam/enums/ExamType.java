package com.tianji.exam.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tianji.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExamType implements BaseEnum {
    TEST(1, "练习"),
    EXAM(2, "考试"),
    ;
    @JsonValue
    @EnumValue
    private final int value;
    private final String desc;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ExamType of(Integer value) {
        if (value == null) {
            return null;
        }
        for (ExamType status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }

    public static String desc(Integer value) {
        ExamType status = of(value);
        return status == null ? "" : status.desc;
    }
}
