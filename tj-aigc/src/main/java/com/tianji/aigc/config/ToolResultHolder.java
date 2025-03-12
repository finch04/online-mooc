package com.tianji.aigc.config;

import java.util.*;

/**
 * 工具结果保持器，用来存储tools中得到的结果，请求id 作为key， value为键值对数据
 *
 * @author zzj
 * @version 1.0
 */
public class ToolResultHolder {

    private static final Map<String, Map<String, Object>> HANDLER_MAP = new HashMap<>();


    private ToolResultHolder() {
    }

    public static void put(String key, String field, Object result) {
        if (!HANDLER_MAP.containsKey(key)) {
            HANDLER_MAP.put(key, new HashMap<>());
        }
        HANDLER_MAP.get(key).put(field, result);
    }

    public static Map<String, Object> get(String key) {
        return HANDLER_MAP.get(key);
    }

    public static Object get(String key, String field) {
        return Optional.ofNullable(HANDLER_MAP.get(key))
                .map(map -> map.get(field))
                .orElse(null);
    }

    public static void remove(String key) {
        HANDLER_MAP.remove(key);
    }


}
