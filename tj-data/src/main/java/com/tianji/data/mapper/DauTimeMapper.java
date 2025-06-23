package com.tianji.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.data.model.po.DauTime;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description：用户活跃数时段Mapper接口
 */
@Mapper
public interface DauTimeMapper extends BaseMapper<DauTime> {

}
