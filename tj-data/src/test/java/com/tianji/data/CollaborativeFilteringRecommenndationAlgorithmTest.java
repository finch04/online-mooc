package com.tianji.data; /**
 * @author fsq
 * @date 2025/6/24 21:14
 */

import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.data.algorithm.CollaborativeFilteringRecommendationAlgorithm;
import com.tianji.data.model.po.UserProfile;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/6/24 21:14
 * @Version: 1.0
 */
@SpringBootTest
public class CollaborativeFilteringRecommenndationAlgorithmTest {

    public static void main(String[] args) {
        CollaborativeFilteringRecommendationAlgorithm algorithm = new CollaborativeFilteringRecommendationAlgorithm();

        // 示例数据
        UserProfile targetUser = new UserProfile();
        targetUser.setUserId(1L);
        targetUser.setCourseLabels(Arrays.asList(1L, 2L));

        UserDTO targetUserInfo = new UserDTO();
        targetUserInfo.setId(1L);
        targetUserInfo.setGender(0);
        targetUserInfo.setProvince("广东");

        UserProfile otherUser = new UserProfile();
        otherUser.setUserId(2L);
        otherUser.setCourseLabels(Arrays.asList(2L, 3L));

        UserDTO otherUserInfo = new UserDTO();
        otherUserInfo.setId(2L);
        otherUserInfo.setGender(0);
        otherUserInfo.setProvince("广东");

        CourseSimpleInfoDTO course1 = new CourseSimpleInfoDTO();
        course1.setId(1L);
        CourseSimpleInfoDTO course2 = new CourseSimpleInfoDTO();
        course2.setId(2L);
        CourseSimpleInfoDTO course3 = new CourseSimpleInfoDTO();
        course3.setId(3L);

        List<UserProfile> allUsers = Arrays.asList(targetUser, otherUser);
        List<UserDTO> allUserInfos = Arrays.asList(targetUserInfo, otherUserInfo);
        List<CourseSimpleInfoDTO> allCourses = Arrays.asList(course1, course2, course3);

        // 推荐课程
        List<CourseSimpleInfoDTO> recommendedCourses = algorithm.recommendCourses(targetUser, targetUserInfo, allUsers, allUserInfos, allCourses);
        System.out.println("推荐课程：" + recommendedCourses);
    }

}
