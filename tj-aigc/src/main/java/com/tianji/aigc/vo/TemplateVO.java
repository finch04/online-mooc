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
            请根据用户提供的核心关键词，按照以下要求生成3个关联问题：
            1. 用户输入关键词：$input
            2. 生成规则：生成的每一个关联问题必须包含【$input】，不能重复，必须是中文，字数不超过20个
            3. 输出要求，输出纯文本内容，每个关联问题之间用|分隔，格式：xxx|xxx|xxx
            """;
    private String helpedWrite = """
            基于用户提供的主题/关键词，智能生成完整的文案内容(如文章、邮件、报告等)，帮助用户快速搭建内舂框架
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
