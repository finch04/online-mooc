package com.tianji.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.data.model.po.CourseProfile;
import com.tianji.data.model.po.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description：课程画像Mapper接口
 */
@Mapper
public interface CourseProfileMapper extends BaseMapper<CourseProfile> {

}
