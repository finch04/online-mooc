package com.tianji.chat.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public  class MarkdownChunk {
        public String title;
        public String content;

        @Override
        public String toString() {
            return "title: " + title + "\ncontent:\n" + content ;
        }

}