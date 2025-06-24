package com.tianji.data;

import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.data.algorithm.FeatureBasedRecommendationAlgorithm;
import com.tianji.data.model.po.CourseProfile;
import com.tianji.data.model.po.UserProfile;
import com.tianji.api.dto.user.UserDTO;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
@SpringBootTest
public class FeatureBasedRecommendationAlgorithmTest {

    public static void main(String[] args) {
        FeatureBasedRecommendationAlgorithm algorithm = new FeatureBasedRecommendationAlgorithm();

        // 用户数据
        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setProvince("广东");
        user.setGender(0); // 男性

        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(1L);
        userProfile.setFreeLabel(0); // 偏好免费课程

        // 课程数据
        List<CourseSimpleInfoDTO> allCourses = Arrays.asList(
                createCourse(1L, "Java编程基础", true),  // 免费课程
                createCourse(2L, "Python数据分析", true), // 免费课程
                createCourse(3L, "Web前端开发", false),  // 付费课程
                createCourse(4L, "机器学习入门", false),  // 付费课程
                createCourse(5L, "大数据技术", true),    // 免费课程
                createCourse(6L, "UI设计实战", false),   // 付费课程
                createCourse(7L, "Android开发", true),   // 免费课程
                createCourse(8L, "iOS开发", false),      // 付费课程
                createCourse(9L, "测试工程师", true),    // 免费课程
                createCourse(10L, "产品经理", false)     // 付费课程
        );

        // 课程画像数据
        List<CourseProfile> courseProfiles = Arrays.asList(
                createCourseProfile(1L, "男性", Arrays.asList("广东", "北京", "上海")),
                createCourseProfile(2L, "男性", Arrays.asList("广东", "江苏", "浙江")),
                createCourseProfile(3L, "男性", Arrays.asList("北京", "上海", "江苏")),
                createCourseProfile(4L, "男性", Arrays.asList("上海", "浙江", "广东")),
                createCourseProfile(5L, "男性", Arrays.asList("广东", "四川", "湖北")),
                createCourseProfile(6L, "女性", Arrays.asList("广东", "北京", "上海")),
                createCourseProfile(7L, "男性", Arrays.asList("广东", "江苏", "浙江")),
                createCourseProfile(8L, "男性", Arrays.asList("北京", "上海", "江苏")),
                createCourseProfile(9L, "女性", Arrays.asList("上海", "浙江", "广东")),
                createCourseProfile(10L, "女性", Arrays.asList("广东", "四川", "湖北"))
        );

        // 获取推荐课程
        List<CourseSimpleInfoDTO> recommendedCourses =
                algorithm.recommendCourses(user, userProfile, allCourses, courseProfiles, 5);

        // 输出推荐结果
        System.out.println("为用户推荐的课程：");
        for (CourseSimpleInfoDTO course : recommendedCourses) {
            System.out.println("- " + course.getName() + " (免费: " + course.getFree() + ")");
        }
    }

    private static CourseSimpleInfoDTO createCourse(Long id, String name, boolean isFree) {
        CourseSimpleInfoDTO course = new CourseSimpleInfoDTO();
        course.setId(id);
        course.setName(name);
        course.setFree(isFree);
        return course;
    }

    private static CourseProfile createCourseProfile(Long courseId, String sexLabel, List<String> provinceLabels) {
        CourseProfile profile = new CourseProfile();
        profile.setCourseId(courseId);
        profile.setSexLabel(sexLabel);
        profile.setProvinceLabels(provinceLabels);
        return profile;
    }
}