/*
 Navicat Premium Dump SQL

 Source Server         : tianji
 Source Server Type    : MySQL
 Source Server Version : 80029 (8.0.29)
 Source Host           : 192.168.150.101:3306
 Source Schema         : tj_data

 Target Server Type    : MySQL
 Target Server Version : 80029 (8.0.29)
 File Encoding         : 65001

 Date: 26/06/2025 19:49:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for course_profile
-- ----------------------------
DROP TABLE IF EXISTS `course_profile`;
CREATE TABLE `course_profile`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `course_id` bigint NULL DEFAULT NULL COMMENT '课程id',
  `sex_label` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '访问用户的性别标签',
  `province_labels` json NULL COMMENT '访问用户的省份标签 只存储前5个',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tab_course_conversion_dpv
-- ----------------------------
DROP TABLE IF EXISTS `tab_course_conversion_dpv`;
CREATE TABLE `tab_course_conversion_dpv`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `do_browse_dpv` bigint NULL DEFAULT NULL COMMENT '课程浏览次数',
  `do_order_dpv` bigint NULL DEFAULT NULL COMMENT '课程下单次数',
  `conversion_rate` double NULL DEFAULT NULL COMMENT '课程转化率',
  `report_time` date NULL DEFAULT NULL COMMENT '统计时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tab_course_detail_gender_duv
-- ----------------------------
DROP TABLE IF EXISTS `tab_course_detail_gender_duv`;
CREATE TABLE `tab_course_detail_gender_duv`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `man_dpv` bigint NULL DEFAULT NULL COMMENT '男：日课程详情访问数',
  `woman_dpv` bigint NULL DEFAULT NULL COMMENT '女：日课程详情访问数',
  `report_time` date NULL DEFAULT NULL COMMENT '统计时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tab_course_detail_province_duv
-- ----------------------------
DROP TABLE IF EXISTS `tab_course_detail_province_duv`;
CREATE TABLE `tab_course_detail_province_duv`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `province_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '省',
  `duv` bigint NULL DEFAULT NULL COMMENT '省日课程用户访问数',
  `report_time` date NULL DEFAULT NULL COMMENT '统计时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tab_dau
-- ----------------------------
DROP TABLE IF EXISTS `tab_dau`;
CREATE TABLE `tab_dau`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `all_dau` bigint NULL DEFAULT NULL COMMENT '总用户活跃数',
  `new_dau` bigint NULL DEFAULT NULL COMMENT '老用户活跃数',
  `old_dau` bigint NULL DEFAULT NULL COMMENT '新用户活跃数',
  `report_time` date NULL DEFAULT NULL COMMENT '统计时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tab_dau_province
-- ----------------------------
DROP TABLE IF EXISTS `tab_dau_province`;
CREATE TABLE `tab_dau_province`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `dau` bigint NULL DEFAULT NULL COMMENT '活跃用户数',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省',
  `report_time` date NULL DEFAULT NULL COMMENT '统计时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_province_report_time`(`province` ASC, `report_time` ASC) USING BTREE,
  INDEX `idx_report_time`(`report_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活跃用户所属省份' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tab_dau_range
-- ----------------------------
DROP TABLE IF EXISTS `tab_dau_range`;
CREATE TABLE `tab_dau_range`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `dau_rang` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '活跃数区间',
  `user_num` bigint NULL DEFAULT NULL COMMENT '用户数',
  `report_time` date NULL DEFAULT NULL COMMENT '统计时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tab_dnu
-- ----------------------------
DROP TABLE IF EXISTS `tab_dnu`;
CREATE TABLE `tab_dnu`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `dnu` bigint NULL DEFAULT NULL COMMENT '日新增用户数',
  `report_time` date NULL DEFAULT NULL COMMENT '统计时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tab_dpv
-- ----------------------------
DROP TABLE IF EXISTS `tab_dpv`;
CREATE TABLE `tab_dpv`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `dpv` bigint NULL DEFAULT NULL COMMENT '日访问页面量',
  `report_time` date NULL DEFAULT NULL COMMENT '统计时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tab_dpv_time
-- ----------------------------
DROP TABLE IF EXISTS `tab_dpv_time`;
CREATE TABLE `tab_dpv_time`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `mid_night_dpv` bigint NULL DEFAULT NULL COMMENT '0~7点活跃访问量数（不包括右区间）',
  `noon_dpv` bigint NULL DEFAULT NULL COMMENT '7~12点活跃访问量数（不包括右区间）',
  `afternoon_dpv` bigint NULL DEFAULT NULL COMMENT '12~18点活跃访问量数（不包括右区间）',
  `evening_dpv` bigint NULL DEFAULT NULL COMMENT '18~24点活跃访问量数（不包括右区间）',
  `report_time` date NULL DEFAULT NULL COMMENT '统计时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_report_time`(`report_time` ASC) USING BTREE COMMENT '确保每个统计时间唯一'
) ENGINE = InnoDB AUTO_INCREMENT = 1937496827599806467 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tab_duv
-- ----------------------------
DROP TABLE IF EXISTS `tab_duv`;
CREATE TABLE `tab_duv`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `duv` bigint NULL DEFAULT NULL COMMENT '日唯一访问量',
  `report_time` date NULL DEFAULT NULL COMMENT '统计时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_profile
-- ----------------------------
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户id',
  `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名称',
  `sex` int NULL DEFAULT NULL COMMENT '性别：0-男性，1-女性',
  `province` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '省',
  `course_labels` json NULL COMMENT '用户偏好  常访问课程id 前5',
  `free_label` int NULL DEFAULT NULL COMMENT '用户偏好  付费课程还是免费课程 0免费1付费',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
