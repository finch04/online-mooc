package com.tianji.chat.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public  class MarkdownChunkVO {
        public String title;
        public String content;
        public Double score;

        public static MarkdownChunk fromString(String str) {
                String[] parts = str.split("\ncontent:\n", 2);
                if (parts.length != 2) {
                        throw new IllegalArgumentException("输入的字符串格式不正确，无法解析为 MarkdownChunk 对象");
                }
                String title = parts[0].substring("title: ".length());
                String content = parts[1];
                return new MarkdownChunk(title, content);
        }

}