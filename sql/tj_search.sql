/*
 Navicat Premium Dump SQL

 Source Server         : tianji
 Source Server Type    : MySQL
 Source Server Version : 80029 (8.0.29)
 Source Host           : 192.168.150.101:3306
 Source Schema         : tj_search

 Target Server Type    : MySQL
 Target Server Version : 80029 (8.0.29)
 File Encoding         : 65001

 Date: 26/06/2025 19:49:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for interests
-- ----------------------------
DROP TABLE IF EXISTS `interests`;
CREATE TABLE `interests`  (
  `id` bigint NOT NULL COMMENT '主键，对应用户id',
  `interests` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '感兴趣的二级分类id，以逗号分隔，例如：120,220,330',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户兴趣表，保存感兴趣的二级分类id' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
