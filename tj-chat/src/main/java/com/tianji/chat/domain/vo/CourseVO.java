package com.tianji.chat.domain.vo;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
public class CourseVO {

  @Description("课程id")
  private Long id;

  @Description("课程完整名称")
  private String name;

  @Description("课程价格")
  private Integer price;

}