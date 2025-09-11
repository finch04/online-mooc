package com.tianji.aigc.domain.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

/**
 * 音频事件过滤参数
 */
@Data
@Builder
public class AudioEventFilterParam {

    /**
     * 过滤模式：
     * INCLUDE - 只保留指定事件类型的内容
     * EXCLUDE - 剔除指定事件类型的内容，保留其他
     */
    private FilterMode filterMode;

    /**
     * 目标事件类型集合（需与阿里云返回的事件标记一致，如"Speech","Applause","BGM","Laughter"）
     */
    private Set<String> targetEventTypes;

    /**
     * 是否保留事件标记（true=保留<|Speech|>这类标记，false=只保留纯文本内容）
     */
    private boolean keepEventTag = false;

    // 过滤模式枚举
    public enum FilterMode {
        INCLUDE, EXCLUDE
    }

    // 默认参数：只保留人声（Speech），不保留标记
    public static AudioEventFilterParam defaultKeepSpeech() {
        return AudioEventFilterParam.builder()
                .filterMode(FilterMode.INCLUDE)
                .targetEventTypes(Set.of("Speech"))
                .keepEventTag(false)
                .build();
    }

    // 默认参数：剔除背景音乐（BGM），保留其他
    public static AudioEventFilterParam defaultExcludeBgm() {
        return AudioEventFilterParam.builder()
                .filterMode(FilterMode.EXCLUDE)
                .targetEventTypes(Set.of("BGM"))
                .keepEventTag(true)
                .build();
    }
}