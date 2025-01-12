package com.tianji.data.model.po;

import lombok.Data;

import java.io.Serializable;

@Data
public class CourseInfo implements Serializable {
    private String category;
    private String name;
    private Integer newStuNum;
    private Double orderAmount;
}
