package com.tianji.aigc.memory.clean;

public interface OriginalDataCleaner {
    /**
     * 删除原始数据源中的聊天记录
     * @param conversationId 对话ID
     */
    void clean(String conversationId);

    /**
     * 获取当前清理除器支持的数据源类型
     */
    DataSourceType getSupportedType();
}