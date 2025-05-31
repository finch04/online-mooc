package com.tianji.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.message.domain.po.MessageReceipt;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageReceiptMapper extends BaseMapper<MessageReceipt> {
    int updateStatusBatch(
        @Param("messageIds") List<Long> messageIds,
        @Param("userId") Long userId,
        @Param("status") Integer status,
        @Param("updateTime") LocalDateTime updateTime);
}