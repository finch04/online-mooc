package com.tianji.chat.domain.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PromptBuilder {
    // 使用固定标记字符串
    public static final String CONTEXT_START = "[SYS_CONTEXT_BEGIN]";
    public static final String CONTEXT_END = "[SYS_CONTEXT_END]";

    // 包装参考资料并附加用户问题
    public static String buildSystemMessage(String context, String message) {
        if (context == null) context = "";
        if (message == null) message = "";

        // 包装参考资料并与用户问题拼接
        return CONTEXT_START + context + CONTEXT_END + message;
    }

    // 解析原始问题（移除标记及其中的内容）
    public static String extractOriginalMessage(String systemMessage) {
        if (systemMessage == null) return "";

        // 使用正则表达式精准匹配并提取用户问题
        String escapedStart = Pattern.quote(CONTEXT_START);
        String escapedEnd = Pattern.quote(CONTEXT_END);

        // 模式：匹配结束标记后的所有内容
        Pattern pattern = Pattern.compile(escapedEnd + "(.*)$", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(systemMessage);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 如果没有找到标记，返回原始消息（可能未经过包装）
        return systemMessage.trim();
    }
}