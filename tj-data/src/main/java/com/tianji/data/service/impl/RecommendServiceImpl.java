package com.tianji.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.utils.UserContext;
import com.tianji.data.algorithm.FeatureBasedRecommendationAlgorithm;
import com.tianji.data.mapper.DnuMapper;
import com.tianji.data.model.po.CourseProfile;
import com.tianji.data.model.po.Dnu;
import com.tianji.data.model.po.UserProfile;
import com.tianji.data.service.ICourseProfileService;
import com.tianji.data.service.IDnuService;
import com.tianji.data.service.IRecommendService;
import com.tianji.data.service.IUserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description：推荐算法服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl  implements IRecommendService {

    private final IUserProfileService userProfileService;
    private final ICourseProfileService courseProfileService;
    private final UserClient userClient;
    private final CourseClient courseClient;

    @Override
    public List<Long> featureRecommend() {
        Long userId = UserContext.getUser();
        UserProfile userProfile = userProfileService.lambdaQuery().eq(UserProfile::getUserId, userId).one();
        List<CourseProfile> courseProfiles = courseProfileService.lambdaQuery().list();
        List<CourseSimpleInfoDTO> allCourses = courseClient.getSimpleInfoList(courseProfiles.stream().map(CourseProfile::getCourseId).collect(Collectors.toList()));
        UserDTO dto = userClient.queryUserById(userId);
        FeatureBasedRecommendationAlgorithm featureBasedRecommendationAlgorithm = new FeatureBasedRecommendationAlgorithm();
        List<CourseSimpleInfoDTO> courseSimpleInfoDTOS = featureBasedRecommendationAlgorithm.recommendCourses(
                dto,
                userProfile,
                allCourses,
                courseProfiles,
                3);
        return courseSimpleInfoDTOS.stream().map(CourseSimpleInfoDTO::getId).collect(Collectors.toList());
    }
}
