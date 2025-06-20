package com.tianji.data.influxdb.tool;

public class UrlRegexConverter {

    /**
     * 将普通URL转换为InfluxDB兼容的正则表达式（带转义）
     * @param url 原始URL（例如：/login）
     * @return InfluxDB正则表达式（例如：\\/login\\/）
     */
    public static String convertToInfluxRegex(String url) {
        if (url == null || url.trim().isEmpty()) {
            return ".*";
        }

        // 1. 标准化URL（保留开头的/，移除结尾的/）
        String normalizedUrl = url.trim();
        if (normalizedUrl.endsWith("/")) {
            normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
        }

        // 2. 转义InfluxDB正则特殊字符
        //    需要转义的字符：. + ? ^ $ {} () | \ [ ] /
        String escapedUrl = normalizedUrl.replaceAll("([.+?^${}()|\\\\\\[\\]/])", "\\\\$1");

        // 3. 为路径添加首尾转义斜杠
        escapedUrl = "\\/" + escapedUrl + "\\/";

        // 4. 处理通配符*（转换为正则的.*）
        escapedUrl = escapedUrl.replaceAll("\\*", ".*");

        return escapedUrl;
    }

    /**
     * 示例：将/login转换为InfluxDB正则表达式
     */
    public static void main(String[] args) {
        String url = "/login";
        String regex = convertToInfluxRegex(url);
        System.out.println("原始URL: " + url);
        System.out.println("转换后正则: " + regex);
        // 输出结果：\\/login\\/
        // 在InfluxDB查询中使用时应为：=~/\\/login\\//
    }
}