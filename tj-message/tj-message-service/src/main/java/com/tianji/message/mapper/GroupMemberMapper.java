package com.tianji.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.message.domain.po.GroupMember;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface GroupMemberMapper extends BaseMapper<GroupMember> {
    int batchInsert(@Param("list") List<GroupMember> members);
    List<Long> selectMemberIdsByGroupId(@Param("groupId") Long groupId);
}