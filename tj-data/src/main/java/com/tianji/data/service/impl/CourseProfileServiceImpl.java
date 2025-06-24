package com.tianji.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.data.mapper.CourseProfileMapper;
import com.tianji.data.mapper.DpvMapper;
import com.tianji.data.model.po.CourseProfile;
import com.tianji.data.model.po.Dpv;
import com.tianji.data.service.ICourseProfileService;
import com.tianji.data.service.IDpvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description：课程画像服务实现类
 */
@Slf4j
@Service
public class CourseProfileServiceImpl extends ServiceImpl<CourseProfileMapper, CourseProfile> implements ICourseProfileService {

}
