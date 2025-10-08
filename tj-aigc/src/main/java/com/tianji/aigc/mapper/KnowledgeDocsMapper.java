package com.tianji.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.aigc.domain.po.KnowledgeDocs;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户上传的 Markdown 文档表 Mapper 接口
 * </p>
 *
 * @author fsq
 * @since 2025-10-08
 */
@Mapper
public interface KnowledgeDocsMapper extends BaseMapper<KnowledgeDocs> {

}
