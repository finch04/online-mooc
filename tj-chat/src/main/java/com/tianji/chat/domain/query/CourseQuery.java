package com.tianji.chat.domain.query;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import java.util.List;

@Data
public class CourseQuery {

  @Description("课程名称")
  private String name;

}