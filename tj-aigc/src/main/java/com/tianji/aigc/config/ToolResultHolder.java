package com.tianji.aigc.config;

import cn.hutool.core.lang.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具结果保持器，用来存储tools中得到的结果，请求id 作为key， value为键值对数据
 *
 * @author zzj
 * @version 1.0
 */
public class ToolResultHolder {

    private static final Map<String, Map<String, Object>> HANDLER_MAP = new ConcurrentHashMap<>();


    /**
     * 工具类，禁止实例化
     */
    private ToolResultHolder() {
    }

    public static void put(String key, String field, Object result) {
        Assert.notNull(key, "key is not null!");
        Assert.notNull(field, "field is not null!");
        HANDLER_MAP.computeIfAbsent(key, k -> new HashMap<>()).put(field, result);
    }

    public static Map<String, Object> get(String key) {
        return key == null ? null : HANDLER_MAP.get(key);
    }

    public static Object get(String key, String field) {
        Assert.notNull(key, "key is not null!");
        Assert.notNull(field, "field is not null!");
        return Optional.ofNullable(HANDLER_MAP.get(key))
                .map(map -> map.get(field))
                .orElse(null);
    }

    public static void remove(String key) {
        Assert.notNull(key, "key is not null!");
        HANDLER_MAP.remove(key);
    }

}
