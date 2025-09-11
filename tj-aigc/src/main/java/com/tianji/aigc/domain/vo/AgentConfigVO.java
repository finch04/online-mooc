package com.tianji.aigc.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfigVO {
    /**
     * 智能体标题
     */
    private String title;
    
    /**
     * 智能体描述
     */
    private String describe;
    
    /**
     * 热门问题列表
     */
    private List<Example> hotQuestions;


    /**
     * Example类表示每个示例的标题和描述。
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Example {

        /**
         * 示例的标题，描述了示例的类型或内容。
         */
        private String title;

        /**
         * 示例的描述，具体说明了示例的内容或问题。
         */
        private String describe;
    }
}