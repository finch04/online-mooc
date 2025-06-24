package com.tianji.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.data.mapper.CourseProfileMapper;
import com.tianji.data.mapper.UserProfileMapper;
import com.tianji.data.model.po.CourseProfile;
import com.tianji.data.model.po.UserProfile;
import com.tianji.data.service.ICourseProfileService;
import com.tianji.data.service.IUserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description：用户画像服务实现类
 */
@Slf4j
@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements IUserProfileService {

}
