package com.tianji.live;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author: fsq
 * @Date: 2025/9/1 16:05
 * @Version: 1.0
 */

public class Test {
    public static void main(String[] args) {
        // 测试用例1：基础场景
        String[] words1 = {"abcw", "baz", "foo", "bar", "xtfn", "abcdef"};
        int expected1 = 16; // "abcw"（4）和"xtfn"（4）的乘积
        int result1 = maxProduct(words1);
        printTestResult(1, expected1, result1);

        // 测试用例2：存在多个重叠的场景
        String[] words2 = {"a", "ab", "abc", "d", "cd", "bcd", "abcd"};
        int expected2 = 4; // "ab"（2）和"cd"（2）或"a"（1）和"bcd"（3）的乘积
        int result2 = maxProduct(words2);
        printTestResult(2, expected2, result2);

        // 测试用例3：无符合条件的单词对
        String[] words3 = {"a", "aa", "aaa", "aaaa"};
        int expected3 = 0; // 所有单词都含'a'，无符合条件的对
        int result3 = maxProduct(words3);
        printTestResult(3, expected3, result3);

        // 测试用例4：只有两个单词且无公共字母
        String[] words4 = {"ab", "cd"};
        int expected4 = 4; // 2*2=4
        int result4 = maxProduct(words4);
        printTestResult(4, expected4, result4);

        // 测试用例5：单词长度差异较大的场景
        String[] words5 = {"abcdefghij", "k", "l", "mnopqrstuvwxyz"};
        int expected5 = 140; // "abcdefghij"（10）和"mnopqrstuvwxyz"（14）的乘积
        int result5 = maxProduct(words5);
        printTestResult(5, expected5, result5);
    }

    // 打印测试结果的辅助方法
    private static void printTestResult(int caseNum, int expected, int actual) {
        String status = (expected == actual) ? "成功" : "失败";
        System.out.printf("测试用例%d: 预期结果=%d, 实际结果=%d, 状态=%s%n",
                caseNum, expected, actual, status);
    }

    public static int maxProduct(String[] words) {
        int length = words.length;
        int[] masks = new int[length];
        for (int i = 0; i < length; i++) {
            String word = words[i];
            int wordLength = word.length();
            for (int j = 0; j < wordLength; j++) {
                masks[i] |= 1 << (word.charAt(j) - 'a');
            }
        }
        int maxProd = 0;
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                if ((masks[i] & masks[j]) == 0) {
                    maxProd = Math.max(maxProd, words[i].length() * words[j].length());
                }
            }
        }
        return maxProd;
    }
}

