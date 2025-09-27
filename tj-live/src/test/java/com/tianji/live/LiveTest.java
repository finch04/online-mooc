package com.tianji.live;

import cn.hutool.core.lang.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @Author: fsq
 * @Date: 2025/6/4 18:58
 * @Version: 1.0
 */
//@SpringBootTest
public class LiveTest {

    public static void main(String[] args) {
        // 测试用例1
        int[] nums1 = {-1, 2, 1, -4};
        int target1 = 1;
        int expected1 = 2;
        int result1 = threeSumClosest(nums1, target1);
        printTestResult(1, expected1, result1);

        // 测试用例2
        int[] nums2 = {0, 2, 1, -3};
        int target2 = 1;
        int expected2 = 0;
        int result2 = threeSumClosest(nums2, target2);
        printTestResult(2, expected2, result2);

        // 测试用例3（精确匹配）
        int[] nums3 = {1, 2, 3, 4};
        int target3 = 6;
        int expected3 = 6;
        int result3 = threeSumClosest(nums3, target3);
        printTestResult(3, expected3, result3);

        // 测试用例4（全正数）
        int[] nums4 = {1, 2, 4, 8};
        int target4 = 11;
        int expected4 = 11;
        int result4 = threeSumClosest(nums4, target4);
        printTestResult(4, expected4, result4);

        // 测试用例5（全负数）
        int[] nums5 = {-5, -3, -2, -1};
        int target5 = -7;
        int expected5 = -8;
        int result5 = threeSumClosest(nums5, target5);
        printTestResult(5, expected5, result5);

        // 测试用例6（最小长度数组）
        int[] nums6 = {1, 2, 3};
        int target6 = 7;
        int expected6 = 6;
        int result6 = threeSumClosest(nums6, target6);
        printTestResult(6, expected6, result6);

        // 测试用例7（大数场景）
        int[] nums7 = {1000, 2000, 3000, 4000};
        int target7 = 8000;
        int expected7 = 8000;
        int result7 = threeSumClosest(nums7, target7);
        printTestResult(7, expected7, result7);
    }

    // 打印测试结果的辅助方法
    private static void printTestResult(int caseNum, int expected, int actual) {
        String status = (expected == actual) ? "成功" : "失败";
        System.out.printf("测试用例%d: 预期结果=%d, 实际结果=%d, 状态=%s%n",
                caseNum, expected, actual, status);
    }

    public static int threeSumClosest(int[] nums, int target) {
        Arrays.sort(nums);
        int ans = nums[0] + nums[1] + nums[2];
        for(int i =0;i<nums.length-2;i++){
            int j = i+1;
            int k = nums.length-1;
            while(j<k){
                int sum = nums[i]+nums[j]+nums[k];
                if(sum== target){
                    return sum;
                }

                if(Math.abs(sum-target)<Math.abs(ans-target)){
                    ans = sum;
                }
                if(sum< target){
                    j++;
                }
                if(sum> target){
                    k--;
                }
            }
        }
        return ans;
    }

}
