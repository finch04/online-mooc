package com.tianji.chat.utils;

import cn.hutool.core.util.StrUtil;
import com.tianji.chat.domain.vo.MarkdownChunk;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class MarkdownSplitter {

    // 解析 Markdown 为标题+正文块
    public static List<MarkdownChunk> splitByH2(String markdown) {
        return MarkdownSplitter.getMarkdownChunksByH(markdown, 2);
    }

    public static List<MarkdownChunk> splitByH3(String markdown) {
        return MarkdownSplitter.getMarkdownChunksByH(markdown, 3);
    }
    public static @NotNull List<MarkdownChunk> getMarkdownChunksByH(String markdown, int level) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        Node document = parser.parse(markdown);
        log.debug("解析后的 Markdown 文档: {}", document.getDocument());
        List<MarkdownChunk> chunks = new ArrayList<>();
        String currentTitle = null;
        StringBuilder currentContent = new StringBuilder();

        for (Node node = document.getFirstChild(); node != null; node = node.getNext()) {
            if (node instanceof Heading) {
                Heading heading = (Heading) node; // 手动类型转换
                if (heading.getLevel() == level) {
                    if (currentTitle != null) {
                        chunks.add(new MarkdownChunk(currentTitle, currentContent.toString().trim()));
                        currentContent = new StringBuilder();
                    }
                    currentTitle = heading.getText().toString().trim(); // ← 转为 String
                }
            } else {
                BasedSequence nodeText = node.getChars();
                if (currentTitle != null) {
                    currentContent.append(nodeText.toString()).append("\n\n");
                }
            }
        }
        // 添加最后一个 chunk
        if (currentTitle != null && StrUtil.isNotEmpty(currentContent)) {
            chunks.add(new MarkdownChunk(currentTitle, currentContent.toString().trim()));
        }
        return chunks;
    }

    public static @NotNull List<MarkdownChunk> smartSplitByHeading(String markdown) {
        // 1. 首先找出文档中最大的标题级别
        int maxLevel = findMaxHeadingLevel(markdown);

        // 2. 如果没有任何标题，将整个文档作为一个块
        if (maxLevel == 0) {
            return Collections.singletonList(new MarkdownChunk("", markdown.trim()));
        }

        // 3. 按最大标题级别分块
        return getMarkdownChunksByH(markdown, maxLevel);
    }

    private static int findMaxHeadingLevel(String markdown) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        Node document = parser.parse(markdown);

        int maxLevel = 0;

        // 使用递归方式查找最大标题级别
        Node node = document.getFirstChild();
        while (node != null) {
            if (node instanceof Heading) {
                Heading heading = (Heading) node;
                if (heading.getLevel() > maxLevel) {
                    maxLevel = heading.getLevel();
                }
            }

            // 检查子节点
            Node child = node.getFirstChild();
            if (child != null) {
                int childMaxLevel = findMaxHeadingLevelInNode(child);
                if (childMaxLevel > maxLevel) {
                    maxLevel = childMaxLevel;
                }
            }

            node = node.getNext();
        }

        return maxLevel;
    }

    private static int findMaxHeadingLevelInNode(Node node) {
        int maxLevel = 0;
        while (node != null) {
            if (node instanceof Heading) {
                Heading heading = (Heading) node;
                if (heading.getLevel() > maxLevel) {
                    maxLevel = heading.getLevel();
                }
            }

            Node child = node.getFirstChild();
            if (child != null) {
                int childMaxLevel = findMaxHeadingLevelInNode(child);
                if (childMaxLevel > maxLevel) {
                    maxLevel = childMaxLevel;
                }
            }

            node = node.getNext();
        }
        return maxLevel;
    }

}
