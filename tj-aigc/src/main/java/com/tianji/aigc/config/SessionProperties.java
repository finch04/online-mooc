package com.tianji.aigc.config;

import com.tianji.aigc.vo.SessionVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "tj.ai.session")
public class SessionProperties {

    /**
     * AI助手的标题，用于显示助手的名称或身份。
     */
    private String title;

    /**
     * AI助手的描述，简要介绍助手的功能或特点。
     */
    private String describe;

    /**
     * 示例列表，包含一些使用助手的示例。
     */
    private List<SessionVO.Example> examples;

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
