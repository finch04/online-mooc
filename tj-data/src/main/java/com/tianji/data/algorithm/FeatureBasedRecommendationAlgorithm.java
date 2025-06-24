package com.tianji.data.algorithm;

import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.data.model.po.CourseProfile;
import com.tianji.data.model.po.UserProfile;
import com.tianji.api.dto.user.UserDTO;

import java.util.*;


/**
 * 推荐算法  单纯权重判断
 */
public class FeatureBasedRecommendationAlgorithm {

    // 省份权重
    private static final double PROVINCE_WEIGHT = 0.3;
    // 性别权重
    private static final double GENDER_WEIGHT = 0.2;
    // 用户偏好权重
    private static final double PREFERENCE_WEIGHT = 0.5;

    /**
     * 推荐课程
     * @param user 用户信息
     * @param userProfile 用户画像
     * @param allCourses 所有课程
     * @param courseProfiles 课程画像
     * @param limit 推荐数量限制
     * @return 推荐的课程列表
     */
    public List<CourseSimpleInfoDTO> recommendCourses(
            UserDTO user,
            UserProfile userProfile,
            List<CourseSimpleInfoDTO> allCourses,
            List<CourseProfile> courseProfiles,
            int limit) {
        
        // 构建课程ID到课程画像的映射
        Map<Long, CourseProfile> courseIdToProfile = new HashMap<>();
        for (CourseProfile profile : courseProfiles) {
            courseIdToProfile.put(profile.getCourseId(), profile);
        }
        
        // 计算每个课程的推荐分数
        List<CourseScore> courseScores = new ArrayList<>();
        for (CourseSimpleInfoDTO course : allCourses) {
            CourseProfile courseProfile = courseIdToProfile.get(course.getId());
            if (courseProfile != null) {
                double score = calculateCourseScore(user, userProfile, course, courseProfile);
                courseScores.add(new CourseScore(course, score));
            }
        }
        
        // 按分数排序并取前limit个
        courseScores.sort((cs1, cs2) -> Double.compare(cs2.score, cs1.score));
        List<CourseSimpleInfoDTO> recommendedCourses = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, courseScores.size()); i++) {
            recommendedCourses.add(courseScores.get(i).course);
        }
        
        return recommendedCourses;
    }
    
    /**
     * 计算课程的推荐分数
     */
    private double calculateCourseScore(
            UserDTO user,
            UserProfile userProfile,
            CourseSimpleInfoDTO course,
            CourseProfile courseProfile) {
        
        // 计算省份匹配分数
        double provinceScore = calculateProvinceScore(user.getProvince(), courseProfile.getProvinceLabels());
        
        // 计算性别匹配分数
        double genderScore = calculateGenderScore(user.getGender(), courseProfile.getSexLabel());
        
        // 计算用户偏好匹配分数
        double preferenceScore = calculatePreferenceScore(userProfile, course);
        
        // 加权求和得到最终分数
        return PROVINCE_WEIGHT * provinceScore +
               GENDER_WEIGHT * genderScore +
               PREFERENCE_WEIGHT * preferenceScore;
    }
    
    /**
     * 计算省份匹配分数
     */
    private double calculateProvinceScore(String userProvince, List<String> courseProvinces) {
        if (userProvince == null || courseProvinces == null || courseProvinces.isEmpty()) {
            return 0.5; // 默认匹配度为50%
        }
        return courseProvinces.contains(userProvince) ? 1.0 : 0.0;
    }
    
    /**
     * 计算性别匹配分数
     */
    private double calculateGenderScore(Integer userGender, String courseGenderLabel) {
        if (userGender == null || courseGenderLabel == null) {
            return 0.5; // 默认匹配度为50%
        }
        
        // 假设courseGenderLabel是"男性"或"女性"，userGender 0为男性，1为女性
        String userGenderStr = userGender == 0 ? "男性" : "女性";
        return courseGenderLabel.contains(userGenderStr) ? 1.0 : 0.0;
    }
    
    /**
     * 计算用户偏好匹配分数
     */
    private double calculatePreferenceScore(UserProfile userProfile, CourseSimpleInfoDTO course) {
        // 检查用户对免费/付费课程的偏好
        boolean userPrefersFree = userProfile.getFreeLabel() == 0;
        boolean courseIsFree = course.getFree();
        
        return userPrefersFree == courseIsFree ? 1.0 : 0.0;
    }
    
    /**
     * 内部类：用于存储课程及其分数
     */
    private static class CourseScore {
        CourseSimpleInfoDTO course;
        double score;
        
        public CourseScore(CourseSimpleInfoDTO course, double score) {
            this.course = course;
            this.score = score;
        }
    }
}