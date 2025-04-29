package com.tianji.learning.mapper;

import com.tianji.learning.domain.po.PointsBoardSeason;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fsq
 * @since 2023-10-26
 */
public interface PointsBoardSeasonMapper extends BaseMapper<PointsBoardSeason> {

    @Insert("  CREATE TABLE `${tableName}`\n" +
            "        (\n" +
            "            `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '榜单id',\n" +
            "            `user_id` BIGINT NOT NULL COMMENT '学生id',\n" +
            "            `points`  INT    NOT NULL COMMENT '积分值',\n" +
            "            PRIMARY KEY (`id`) USING BTREE,\n" +
            "            INDEX `idx_user_id` (`user_id`) USING BTREE\n" +
            "        )\n" +
            "            COMMENT ='学霸天梯榜'\n" +
            "            COLLATE = 'utf8mb4_0900_ai_ci'\n" +
            "            ENGINE = InnoDB\n" +
            "            ROW_FORMAT = DYNAMIC")
    void createPointsBoardTable(@Param("tableName") String tableName);
}
