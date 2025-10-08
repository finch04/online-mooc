package com.tianji.aigc.utils;

import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.config.MarkdownSplitterProperties;
import com.tianji.aigc.domain.po.MarkdownChunk;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarkdownSplitter {
    // 注入配置类（支持动态调整分片参数）
    private final MarkdownSplitterProperties splitterProperties;

    // 段落分隔符正则（匹配两个及以上换行，兼容 Windows/macOS 换行格式）
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\r?\\n\\r?\\n+");
    // 句子分隔符正则（匹配中文句号、感叹号、问号，用于段落内拆分）
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("([。！？；])");

    /**
     * 按指定标题级别分片（新增长度限制）
     */
    public List<MarkdownChunk> splitByH2(String markdown) {
        return getMarkdownChunksByH(markdown, 2);
    }

    public List<MarkdownChunk> splitByH3(String markdown) {
        return getMarkdownChunksByH(markdown, 3);
    }

    public MarkdownSplitterProperties getSplitterProperties(){
        return splitterProperties;
    }

    /**
     * 核心方法：按指定标题级别分片 + 长度限制拆分
     */
    public @NotNull List<MarkdownChunk> getMarkdownChunksByH(String markdown, int level) {
        if (StrUtil.isEmpty(markdown)) {
            log.warn("Markdown 内容为空，返回空分片列表");
            return Collections.emptyList();
        }

        // 1. 初始化 Markdown 解析器
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        Node document = parser.parse(markdown);

        List<MarkdownChunk> finalChunks = new ArrayList<>();
        String currentTitle = null;
        StringBuilder currentContent = new StringBuilder();

        // 2. 遍历 AST 节点，按标题分组内容
        for (Node node = document.getFirstChild(); node != null; node = node.getNext()) {
            if (node instanceof Heading heading) {
                // 遇到目标级别标题：先处理上一个标题的内容
                if (currentTitle != null && currentContent.length() > 0) {
                    // 对当前标题下的内容进行“长度限制拆分”，并添加到最终列表
                    List<String> splitContents = splitContentBySize(currentContent.toString().trim());
                    for (String splitContent : splitContents) {
                        if (splitContent.length() >= splitterProperties.getMinChunkSize()) {
                            finalChunks.add(new MarkdownChunk(currentTitle, splitContent));
                        } else {
                            log.debug("分片内容过短（{}字符），跳过：{}", splitContent.length(), splitContent);
                        }
                    }
                    // 重置当前内容
                    currentContent = new StringBuilder();
                }
                // 更新当前标题（仅目标级别标题生效）
                if (heading.getLevel() == level) {
                    currentTitle = heading.getText().toString().trim();
                }
            } else {
                // 非标题节点：累加内容（保留 Markdown 格式）
                BasedSequence nodeText = node.getChars();
                if (currentTitle != null && nodeText != null && nodeText.length() > 0) {
                    currentContent.append(nodeText.toString()).append("\n\n");
                }
            }
        }

        // 3. 处理最后一个标题的内容
        if (currentTitle != null && currentContent.length() > 0) {
            List<String> splitContents = splitContentBySize(currentContent.toString().trim());
            for (String splitContent : splitContents) {
                if (splitContent.length() >= splitterProperties.getMinChunkSize()) {
                    finalChunks.add(new MarkdownChunk(currentTitle, splitContent));
                }
            }
        }

        log.info("按 H{} 级别分片完成，最终分片数：{}（原始标题分组数：{}）",
                level, finalChunks.size(), countTitleGroups(markdown, level));
        return finalChunks;
    }

    /**
     * 智能分片：先找最大标题级别，再按级别分片 + 长度限制
     */
    public @NotNull List<MarkdownChunk> smartSplitByHeading(String markdown) {
        if (StrUtil.isEmpty(markdown)) {
            return Collections.singletonList(new MarkdownChunk("全文", ""));
        }

        // 1. 找到文档中“最小的标题级别”（即最大的标题权重，如 H1 > H2）
        int maxLevel = findMaxHeadingLevel(markdown);
        log.debug("智能分片：找到最大标题级别 H{}", maxLevel);

        // 2. 无标题：按长度直接拆分全文
        if (maxLevel == 0) {
            List<String> splitContents = splitContentBySize(markdown.trim());
            List<MarkdownChunk> chunks = new ArrayList<>();
            for (int i = 0; i < splitContents.size(); i++) {
                String title = splitContents.size() == 1 ? "全文" : "全文-" + (i + 1);
                chunks.add(new MarkdownChunk(title, splitContents.get(i)));
            }
            return chunks;
        }

        // 3. 有标题：按最大标题级别分片 + 长度限制
        return getMarkdownChunksByH(markdown, maxLevel);
    }

    /**
     * 核心辅助方法：按长度限制拆分内容（支持段落优先、语义保留）
     * @param content 待拆分的文本内容
     * @return 拆分后的内容列表
     */
    private List<String> splitContentBySize(String content) {
        List<String> result = new ArrayList<>();
        int maxSize = splitterProperties.getMaxChunkSize();
        int overlapSize = splitterProperties.getOverlapSize();

        // 内容未超过限制：直接返回
        if (content.length() <= maxSize) {
            result.add(content);
            return result;
        }

        // 1. 优先按段落拆分（保留天然语义分隔）
        String[] paragraphs = PARAGRAPH_PATTERN.split(content);
        List<String> tempParagraphs = new ArrayList<>();
        StringBuilder currentPara = new StringBuilder();

        for (String paragraph : paragraphs) {
            String paraTrimmed = paragraph.trim();
            if (StrUtil.isEmpty(paraTrimmed)) {
                continue;
            }

            // 段落本身超过限制：先拆分段落
            if (paraTrimmed.length() > maxSize) {
                // 先处理当前累积的段落
                if (currentPara.length() > 0) {
                    tempParagraphs.add(currentPara.toString());
                    currentPara.setLength(0);
                }
                // 拆分超长段落（按句子拆分，保留语义）
                List<String> splitParagraphs = splitLongParagraph(paraTrimmed);
                tempParagraphs.addAll(splitParagraphs);
            } else {
                // 段落未超限制：判断累积后是否超限制
                if (currentPara.length() + paraTrimmed.length() + 2 <= maxSize) { // +2 是换行符长度
                    currentPara.append(paraTrimmed).append("\n\n");
                } else {
                    // 累积超限制：添加当前累积段落，重置累积器
                    tempParagraphs.add(currentPara.toString().trim());
                    currentPara = new StringBuilder(paraTrimmed).append("\n\n");
                }
            }
        }

        // 添加最后累积的段落
        if (currentPara.length() > 0) {
            tempParagraphs.add(currentPara.toString().trim());
        }

        // 2. 处理拆分后的段落（可能仍有超限制的，按固定长度+重叠拆分）
        for (String para : tempParagraphs) {
            if (para.length() <= maxSize) {
                result.add(para);
            } else {
                // 固定长度拆分（保留重叠，避免切断语义）
                int start = 0;
                while (start < para.length()) {
                    int end = Math.min(start + maxSize, para.length());
                    // 最后一个分片：直接取到末尾
                    if (end == para.length()) {
                        result.add(para.substring(start));
                        break;
                    }
                    // 非最后一个分片：保留重叠部分
                    result.add(para.substring(start, end));
                    // 更新起始位置（减去重叠长度，避免语义断裂）
                    start = end - overlapSize;
                    // 防止死循环（重叠长度过大时）
                    if (start >= end) {
                        start = end;
                    }
                }
            }
        }

        return result;
    }

    /**
     * 辅助方法：拆分超长段落（按句子拆分，保留中文语义）
     */
    private List<String> splitLongParagraph(String paragraph) {
        List<String> sentences = new ArrayList<>();
        Matcher matcher = SENTENCE_PATTERN.matcher(paragraph);
        StringBuilder currentSentence = new StringBuilder();

        while (matcher.find()) {
            // 保留句子结尾的标点符号
            currentSentence.append(paragraph, matcher.start(), matcher.end());
            String sentence = currentSentence.toString().trim();
            if (StrUtil.isNotEmpty(sentence)) {
                sentences.add(sentence);
            }
            currentSentence.setLength(0);
        }

        // 添加最后一个句子
        if (currentSentence.length() > 0) {
            String lastSentence = currentSentence.toString().trim();
            if (StrUtil.isNotEmpty(lastSentence)) {
                sentences.add(lastSentence);
            }
        }

        // 若拆分后的句子仍超限制：按固定长度拆分（兜底）
        List<String> result = new ArrayList<>();
        int maxSize = splitterProperties.getMaxChunkSize();
        int overlapSize = splitterProperties.getOverlapSize();

        for (String sentence : sentences) {
            if (sentence.length() <= maxSize) {
                result.add(sentence);
            } else {
                int start = 0;
                while (start < sentence.length()) {
                    int end = Math.min(start + maxSize, sentence.length());
                    result.add(sentence.substring(start, end));
                    start = end - overlapSize;
                    if (start >= end) {
                        start = end;
                    }
                }
            }
        }

        return result;
    }

    /**
     * 辅助方法：统计原始标题分组数（用于日志对比）
     */
    private int countTitleGroups(String markdown, int level) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        Node document = parser.parse(markdown);

        int count = 0;
        boolean hasCurrentGroup = false;

        for (Node node = document.getFirstChild(); node != null; node = node.getNext()) {
            if (node instanceof Heading heading && heading.getLevel() == level) {
                count++;
                hasCurrentGroup = true;
            } else if (hasCurrentGroup && node.getNext() == null) {
                // 最后一个分组
                count = Math.max(count, 1);
            }
        }

        return count;
    }

    /**
     * 辅助方法：找到文档中最大的标题级别（H1 级别最高，H6 最低）
     */
    private int findMaxHeadingLevel(String markdown) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        Node document = parser.parse(markdown);

        int maxLevel = 0;
        Node node = document.getFirstChild();

        while (node != null) {
            maxLevel = Math.max(maxLevel, getMaxLevelFromNode(node));
            node = node.getNext();
        }

        return maxLevel;
    }

    /**
     * 递归遍历节点，获取最大标题级别
     */
    private int getMaxLevelFromNode(Node node) {
        int maxLevel = 0;
        if (node instanceof Heading heading) {
            maxLevel = heading.getLevel();
        }

        // 递归处理子节点
        Node child = node.getFirstChild();
        while (child != null) {
            int childMaxLevel = getMaxLevelFromNode(child);
            maxLevel = Math.max(maxLevel, childMaxLevel);
            child = child.getNext();
        }

        return maxLevel;
    }
}