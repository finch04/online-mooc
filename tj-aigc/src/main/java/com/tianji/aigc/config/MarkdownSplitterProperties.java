package com.tianji.aigc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Markdown 分片配置类（可在 application.yml 中配置）
 */
@Component
@ConfigurationProperties(prefix = "tj.ai.splitter")
@Data
public class MarkdownSplitterProperties {
    /**
     * 单个分片最大字符数（默认 2000，根据 Embedding 模型最大输入长度调整，如 BGE 模型建议 ≤ 512/1024  tokens，约 2000 中文字符）
     */
    private int maxChunkSize = 2000;

    /**
     * 分片内容重叠字符数（默认 100，避免拆分时切断语义，如“产品功能说明”被拆成两部分）
     */
    private int overlapSize = 100;

    /**
     * 最小分片字符数（默认 100，避免生成过短的无意义分片）
     */
    private int minChunkSize = 100;
}