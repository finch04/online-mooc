package com.tianji.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.message.domain.po.MessageTemplate;
import com.tianji.message.domain.po.UserConversation;
import com.tianji.message.domain.query.UserConversationQuery;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 第三方短信平台签名和模板信息 Mapper 接口
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-20
 */
public interface UserConversationMapper extends BaseMapper<UserConversation> {
    /**
     * 查询用户会话列表（基础信息）
     * @param page 分页参数
     * @param query 查询条件（包含currentUserId）
     * @return 分页会话列表
     */
    IPage<UserConversation> selectConversationList(
            @Param("page") Page<?> page,
            @Param("userId") Long userId,
            @Param("query") UserConversationQuery query
    );
}
