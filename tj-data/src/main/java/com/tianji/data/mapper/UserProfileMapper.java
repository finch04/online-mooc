package com.tianji.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.data.model.po.Dau;
import com.tianji.data.model.po.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description：用户画像Mapper接口
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

}
