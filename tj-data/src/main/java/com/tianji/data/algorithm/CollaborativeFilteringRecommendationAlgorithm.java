package com.tianji.data.algorithm;

import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.data.model.po.UserProfile;
import com.tianji.api.dto.user.UserDTO;

import java.util.*;

/**
 * 推荐算法   协同过滤算法
 */
public class CollaborativeFilteringRecommendationAlgorithm {

    // 性别权重
    private static final double GENDER_WEIGHT = 0.2;
    // 省份权重
    private static final double PROVINCE_WEIGHT = 0.3;
    // 课程偏好权重
    private static final double COURSE_PREFERENCE_WEIGHT = 0.5;

    // 计算用户之间的相似度
    private double calculateSimilarity(UserProfile user1, UserProfile user2, UserDTO user1Info, UserDTO user2Info) {
        double genderSimilarity = user1Info.getGender().equals(user2Info.getGender()) ? 1 : 0;
        double provinceSimilarity = user1Info.getProvince().equals(user2Info.getProvince()) ? 1 : 0;
        double courseSimilarity = calculateCourseSimilarity(user1.getCourseLabels(), user2.getCourseLabels());

        return GENDER_WEIGHT * genderSimilarity +
                PROVINCE_WEIGHT * provinceSimilarity +
                COURSE_PREFERENCE_WEIGHT * courseSimilarity;
    }

    // 计算课程偏好的相似度
    private double calculateCourseSimilarity(List<Long> courseLabels1, List<Long> courseLabels2) {
        if (courseLabels1 == null || courseLabels2 == null) {
            return 0;
        }
        Set<Long> set1 = new HashSet<>(courseLabels1);
        Set<Long> set2 = new HashSet<>(courseLabels2);
        Set<Long> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        return (double) intersection.size() / (set1.size() + set2.size() - intersection.size());
    }

    // 推荐课程
    public List<CourseSimpleInfoDTO> recommendCourses(UserProfile targetUser, UserDTO targetUserInfo,
                                                      List<UserProfile> allUsers, List<UserDTO> allUserInfos,
                                                      List<CourseSimpleInfoDTO> allCourses) {
        // 计算目标用户与其他用户的相似度
        Map<UserProfile, Double> similarityMap = new HashMap<>();
        for (int i = 0; i < allUsers.size(); i++) {
            UserProfile otherUser = allUsers.get(i);
            UserDTO otherUserInfo = allUserInfos.get(i);
            if (!otherUser.getUserId().equals(targetUser.getUserId())) {
                double similarity = calculateSimilarity(targetUser, otherUser, targetUserInfo, otherUserInfo);
                similarityMap.put(otherUser, similarity);
            }
        }

        // 按相似度排序
        List<Map.Entry<UserProfile, Double>> sortedEntries = new ArrayList<>(similarityMap.entrySet());
        sortedEntries.sort(Map.Entry.<UserProfile, Double>comparingByValue().reversed());

        // 收集相似用户喜欢的课程
        Set<Long> recommendedCourseIds = new HashSet<>();
        for (Map.Entry<UserProfile, Double> entry : sortedEntries) {
            UserProfile similarUser = entry.getKey();
            List<Long> courseLabels = similarUser.getCourseLabels();
            if (courseLabels != null) {
                recommendedCourseIds.addAll(courseLabels);
            }
        }

        // 移除目标用户已经喜欢的课程
        if (targetUser.getCourseLabels() != null) {
            recommendedCourseIds.removeAll(targetUser.getCourseLabels());
        }

        // 获取推荐课程
        List<CourseSimpleInfoDTO> recommendedCourses = new ArrayList<>();
        for (CourseSimpleInfoDTO course : allCourses) {
            if (recommendedCourseIds.contains(course.getId())) {
                recommendedCourses.add(course);
            }
        }

        return recommendedCourses;
    }
}