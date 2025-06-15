package com.tianji.learning.utils;

public class ShortCodeUtil {
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = CHARS.length();

    /**
     * ID转短码（62进制编码）
     */
    public static String encode(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID must be positive");
        }

        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(CHARS.charAt((int) (id % BASE)));
            id /= BASE;
        }

        // 不足6位补前导字符（保证短码长度一致）
        while (sb.length() < 6) {
            sb.append(CHARS.charAt(0));
        }

        return sb.reverse().toString();
    }

    /**
     * 短码转ID（62进制解码）
     */
    public static long decode(String shortCode) {
        long result = 0;
        for (char c : shortCode.toCharArray()) {
            result = result * BASE + CHARS.indexOf(c);
        }
        return result;
    }
}