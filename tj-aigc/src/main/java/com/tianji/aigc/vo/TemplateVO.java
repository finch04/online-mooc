package com.tianji.aigc.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVO {

    private String associationalWord = """
            用户输入关键词：$input|生成规则：生成3个，每个问题含【$input】不超过20字|输出要求：纯文本，问题间用|分隔
            """;
    private String helpedWrite = """
            基于用户提供的主题/关键词，智能生成完整的文案内容（如文章、邮件、报告等），帮助用户快速搭建内容框架
            用户输入：
            $input
            """;

    private String continuedWrite = """
            在用户已有文本基础上，自动延续写作思路生成后续内容，保持上下文逻辑连贯性
            用户输入：
            $input
            """;

    private String polish = """
            对现有文本进行语言优化，包括调整句式结构、替换精准词汇、统一行文风格等
            用户输入：
            $input
            """;

    private String streamline = """
            通过语义分析智能提炼核心信息，删除几余表达，将长文本压缩为简洁版本
            用户输入：
            $input
            """;
}