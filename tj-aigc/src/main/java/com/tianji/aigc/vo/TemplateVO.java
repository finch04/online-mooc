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
            AI帮写
            """;

    private String continuedWrite = """
            AI续写
            """;

    private String polish = """
            AI润色
            """;

    private String streamline = """
            AI精简
            """;
}
