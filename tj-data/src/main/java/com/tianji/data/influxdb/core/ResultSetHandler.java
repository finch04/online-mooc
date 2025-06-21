package com.tianji.data.influxdb.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tianji.common.utils.EmptyUtil;
import com.tianji.data.utils.BeanConv;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 结果集处理器
 */
public class ResultSetHandler {

    /***
     * @description 结果处理
     *
     * @param reultList influx返回结果
     * @param method 目标方法
     * @param sql 执行sql
     * @param resultType 注解声明返回类型
     * @return
     * @return: java.lang.Object
     */
    @SneakyThrows
    public Object handleResultSet(List<Map<String,Object>> reultList, Method method, String sql, Class resultType) {
        Class<?> returnTypeTarget = method.getReturnType();
        //如果结果为空直接返回空构建
        if (EmptyUtil.isNullOrEmpty(reultList)){
            if (returnTypeTarget== List.class){
                return Lists.newArrayList();
            }else if (returnTypeTarget==Map.class){
                return Maps.newHashMap();
            }else if (returnTypeTarget==String.class){
                return null;
            }else {
                return convertStringToObject(resultType,"0");
            }
        }

        //当前method声明返回结果不为list,且resultType与method声明返回结果类型不匹配
        if (returnTypeTarget!= List.class&&resultType!=returnTypeTarget){
            throw  new RuntimeException("返回类型与声明返回类型不匹配");
        }

        //当前method声明返回结果不为list,且resultType与method声明返回结果类型匹配
        if (returnTypeTarget!= List.class&&resultType==returnTypeTarget){
            //结果不唯一则抛出异常
            if (reultList.size()!=1){
                throw  new RuntimeException("返回结果不唯一");
            }
            //驼峰处理
            Map<String, Object> mapHandler = convertKeysToCamelCase(reultList.get(0));
            //单个Map类型
            if (resultType==Map.class){
                return mapHandler;
                //单个自定义类型
            } else if (!isTargetClass(resultType)){
                return BeanConv.toBean(mapHandler, resultType);
                //单个JDK提供指定类型
            }else {
                // 查找第一个非time的字段
                Object value = findFirstNonTimeValue(mapHandler);
                if (value != null) {
                    String target = String.valueOf(value).replace(".0","");
                    return convertStringToObject(resultType, target);
                }
                return convertStringToObject(resultType, "0");
            }
        }

        //当前method声明返回结果为list
        if (returnTypeTarget== List.class){
            //驼峰处理
            List<Map<String, Object>> listHandler = convertKeysToCamelCase(reultList);
            //list的内部为map结果
            if (resultType==Map.class){
                return listHandler;
                //list的内部为自定义类型
            }else if (!isTargetClass(resultType)){
                return BeanConv.toBeanList(listHandler, resultType);
                //list的内部为JDK提供指定类型
            }else {
                List<Object> listResult = Lists.newArrayList();
                for (Map<String, Object> mapHandler : listHandler) {
                    // 查找第一个非time的字段
                    Object value = findFirstNonTimeValue(mapHandler);
                    if (value != null) {
                        String target = String.valueOf(value).replace(".0","");
                        listResult.add(convertStringToObject(resultType, target));
                    } else {
                        listResult.add(convertStringToObject(resultType, "0"));
                    }
                }
                return listResult;
            }
        }
        return  null;
    }

    // 查找Map中第一个非time的字段值
    private Object findFirstNonTimeValue(Map<String, Object> map) {
        for (String key : map.keySet()) {
            if (!key.equals("time") && !EmptyUtil.isNullOrEmpty(map.get(key))) {
                return map.get(key);
            }
        }
        return null;
    }

    // 检查类是否是目标类型
    public static boolean isTargetClass(Class<?> clazz) {
        return clazz == Integer.class ||
                clazz == int.class ||
                clazz == Long.class ||
                clazz == long.class ||
                clazz == Float.class ||
                clazz == float.class ||
                clazz == Double.class ||
                clazz == double.class ||
                clazz == Short.class ||
                clazz == short.class ||
                clazz == Byte.class ||
                clazz == byte.class ||
                clazz == Character.class ||
                clazz == char.class ||
                clazz == Boolean.class||
                clazz == boolean.class||
                clazz== BigDecimal.class ||
                clazz== String.class;
    }

    public static Map<String, Object> convertKeysToCamelCase(Map<String, Object> map) {
        Map<String, Object> camelCaseMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String originalKey = entry.getKey();
            Object value = entry.getValue();
            String camelCaseKey = convertToCamelCase(originalKey);

            camelCaseMap.put(camelCaseKey, value);
        }

        return camelCaseMap;
    }

    public static List<Map<String, Object>> convertKeysToCamelCase(List<Map<String, Object>> mapList) {
        List<Map<String, Object>> listHandler = Lists.newArrayList();
        mapList.forEach(n->{
            listHandler.add(convertKeysToCamelCase(n));
        });
        return listHandler;
    }

    public static String convertToCamelCase(String snakeCase) {
        StringBuilder camelCase = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < snakeCase.length(); i++) {
            char currentChar = snakeCase.charAt(i);
            if (currentChar == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    camelCase.append(Character.toUpperCase(currentChar));
                    nextUpperCase = false;
                } else {
                    camelCase.append(Character.toLowerCase(currentChar));
                }
            }
        }
        return camelCase.toString();
    }

    @SneakyThrows
    public static <T> T convertStringToObject(Class<?> clazz, String str){
        if (clazz == String.class) {
            return (T)str; // 如果目标类型是 String，则直接返回字符串
        } else if (isTargetClass(clazz)){
            // 获取目标类型的构造函数，参数为 String 类型的参数
            Constructor<?> constructor = clazz.getConstructor(String.class);
            return (T)constructor.newInstance(str); // 使用构造函数创建目标类型的对象
        }else {
            return (T)clazz.newInstance();
        }
    }
}