package com.tianji.aigc.enums;

import lombok.Getter;

/**
 * 图片生成风格枚举类
 */
@Getter
public enum ImageStyleEnum {
    AUTO("<auto>", "默认风格，由模型随机输出"),
    PHOTOGRAPHY("<photography>", "摄影风格"),
    PORTRAIT("<portrait>", "人像写真风格"),
    CARTOON_3D("<3d cartoon>", "3D卡通风格"),
    ANIME("<anime>", "动画风格"),
    OIL_PAINTING("<oil painting>", "油画风格"),
    WATERCOLOR("<watercolor>", "水彩风格"),
    SKETCH("<sketch>", "素描风格"),
    CHINESE_PAINTING("<chinese painting>", "中国画风格"),
    FLAT_ILLUSTRATION("<flat illustration>", "扁平插画风格");

    private final String code;
    private final String description;

    ImageStyleEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取枚举
     */
    public static ImageStyleEnum getByCode(String code) {
        for (ImageStyleEnum style : values()) {
            if (style.code.equals(code)) {
                return style;
            }
        }
        return AUTO; // 默认返回自动风格
    }
}