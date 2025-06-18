package com.tianji.learning.utils;

import java.math.BigInteger;

public class ShortCodeUtil {
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = CHARS.length();

    /**
     * 字符串转短码（62进制编码）
     */
    public static String encode(String input) {
        // 将字符串转换为 BigInteger
        BigInteger num = new BigInteger(input, 16); // 假设输入是十六进制字符串
        if (num.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Input must be positive");
        }

        StringBuilder sb = new StringBuilder();
        while (num.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divmod = num.divideAndRemainder(BigInteger.valueOf(BASE));
            sb.append(CHARS.charAt(divmod[1].intValue()));
            num = divmod[0];
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