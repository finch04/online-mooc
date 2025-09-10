package com.tianji.aigc.utils;

import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.entity.dto.AudioEventFilterParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 音频事件解析与过滤工具
 */
public class AudioEventFilterUtil {
    /**
     * 正则表达式：匹配阿里云音频事件块
     * 匹配格式：<|事件类型|>内容<|/事件类型|> （如<|Speech|>你好<|/Speech|>）
     * 分组说明：
     * group1: 事件类型（如Speech/Applause）
     * group2: 事件内容（如"你好"）
     * group3: 完整事件块（如"<|Speech|>你好<|/Speech|>"）
     */
    private static final Pattern EVENT_BLOCK_PATTERN =
            Pattern.compile("<\\|([A-Za-z]+)\\|>(.*?)<\\|/\\1\\|>", Pattern.DOTALL);

    /**
     * 对阿里云STT结果进行事件过滤
     * @param sttResult 阿里云返回的带事件标记的文本（如"<|Speech|>我给你打死。<|/Speech|>"）
     * @param filterParam 过滤参数
     * @return 过滤后的文本
     */
    public static String filter(String sttResult, AudioEventFilterParam filterParam) {
        // 1. 空值处理
        if (StrUtil.isBlank(sttResult) || filterParam == null) {
            return sttResult;
        }

        // 2. 解析所有事件块
        List<EventBlock> eventBlocks = parseEventBlocks(sttResult);

        // 3. 根据过滤参数筛选事件块
        List<EventBlock> filteredBlocks = filterEventBlocks(eventBlocks, filterParam);

        // 4. 重组筛选后的内容（按原顺序拼接）
        return rebuildFilteredContent(filteredBlocks, filterParam.isKeepEventTag());
    }

    /**
     * 解析STT结果中的所有事件块
     */
    private static List<EventBlock> parseEventBlocks(String sttResult) {
        List<EventBlock> eventBlocks = new ArrayList<>();
        Matcher matcher = EVENT_BLOCK_PATTERN.matcher(sttResult);

        while (matcher.find()) {
            String eventType = matcher.group(1); // 事件类型（如Speech）
            String content = matcher.group(2);   // 事件内容（如"我给你打死。"）
            String fullBlock = matcher.group(0); // 完整事件块（带标记）
            eventBlocks.add(new EventBlock(eventType, content, fullBlock));
        }

        // 处理非事件块内容（如事件标记外的纯文本，可选）
        handleNonEventContent(sttResult, eventBlocks);

        return eventBlocks;
    }

    /**
     * 处理非事件块内容（如"|/Applause|天气好棒啊！"中的"天气好棒啊！"）
     */
    private static void handleNonEventContent(String sttResult, List<EventBlock> eventBlocks) {
        // 暂存所有事件块的起始和结束索引，提取剩余文本
        // （根据实际需求决定是否保留非事件内容，此处略，可自行扩展）
    }

    /**
     * 根据过滤参数筛选事件块
     */
    private static List<EventBlock> filterEventBlocks(List<EventBlock> eventBlocks, AudioEventFilterParam filterParam) {
        List<EventBlock> filteredBlocks = new ArrayList<>();
        Set<String> targetTypes = filterParam.getTargetEventTypes();
        AudioEventFilterParam.FilterMode mode = filterParam.getFilterMode();

        for (EventBlock block : eventBlocks) {
            boolean isTarget = targetTypes.contains(block.getEventType());
            // 包含模式：只保留目标事件；排除模式：剔除目标事件
            if ((mode == AudioEventFilterParam.FilterMode.INCLUDE && isTarget)
                    || (mode == AudioEventFilterParam.FilterMode.EXCLUDE &&!isTarget)) {
                filteredBlocks.add(block);
            }
        }

        return filteredBlocks;
    }

    /**
     * 重组筛选后的内容（保留/剔除事件标记）
     */
    private static String rebuildFilteredContent(List<EventBlock> filteredBlocks, boolean keepEventTag) {
        StringBuilder sb = new StringBuilder();
        for (EventBlock block : filteredBlocks) {
            if (keepEventTag) {
                sb.append(block.getFullBlock()); // 保留完整事件块（带标记）
            } else {
                sb.append(block.getContent());   // 只保留纯文本内容
            }
        }
        return sb.toString().trim();
    }

    /**
     * 事件块模型（存储解析后的事件信息）
     */
    private static class EventBlock {
        private final String eventType;  // 事件类型（如Speech）
        private final String content;    // 事件内容（纯文本）
        private final String fullBlock;  // 完整事件块（带标记）

        public EventBlock(String eventType, String content, String fullBlock) {
            this.eventType = eventType;
            this.content = content;
            this.fullBlock = fullBlock;
        }

        // Getter
        public String getEventType() { return eventType; }
        public String getContent() { return content; }
        public String getFullBlock() { return fullBlock; }
    }
}