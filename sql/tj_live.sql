/*
 Navicat Premium Dump SQL

 Source Server         : tianji
 Source Server Type    : MySQL
 Source Server Version : 80029 (8.0.29)
 Source Host           : 192.168.150.101:3306
 Source Schema         : tj_live

 Target Server Type    : MySQL
 Target Server Version : 80029 (8.0.29)
 File Encoding         : 65001

 Date: 25/10/2025 18:19:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for live_room
-- ----------------------------
DROP TABLE IF EXISTS `live_room`;
CREATE TABLE `live_room`  (
  `id` bigint NOT NULL DEFAULT 0 COMMENT '直播间id',
  `stream_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '直播间串流密钥',
  `anchor_id` bigint NOT NULL COMMENT '主播用户id',
  `room_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '直播间标题',
  `room_cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '直播间封面图URL',
  `room_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '直播间描述',
  `room_notice` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '直播间公告',
  `category_id` bigint NULL DEFAULT NULL COMMENT '直播间分类id',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '直播间状态(0-未开播,1-直播中,2-已关闭,3-禁播)',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始直播时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束直播时间',
  `online_count` int NOT NULL DEFAULT 0 COMMENT '当前在线人数',
  `max_online_count` int NOT NULL DEFAULT 0 COMMENT '最高在线人数',
  `like_count` bigint NOT NULL DEFAULT 0 COMMENT '点赞总数',
  `share_count` int NOT NULL DEFAULT 0 COMMENT '分享次数',
  `is_private` bit(1) NULL DEFAULT b'0' COMMENT '是否私有直播间(0-公开,1-私有)',
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '私有直播间密码',
  `hidden` bit(1) NULL DEFAULT b'0' COMMENT '是否被隐藏',
  `hidden_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '隐藏原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_anchor_id`(`anchor_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '直播间表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of live_room
-- ----------------------------
INSERT INTO `live_room` VALUES (1, 'test', 5, '测试用官方直播间', 'http://8.152.192.90/static/img/84e8f0db71f733ed30aa4b43559cd2a9.wallhaven-4y11yd.webp', '官方直播间这是直播间描述', '本平台直播内容及互动评论须严格遵守直播规范，严禁传播违法违规、低俗血暴、吸烟酗酒、造谣诈骗等不良有害信息。如有违规，平台将进行封禁直至永久封停账号哦！注意理性打赏，未成年不提倡大额打赏。请勿轻信平台上各类广告信息，谨防上当受骗。', NULL, 1, NULL, NULL, 423, 0, 45, 322, b'1', '1', b'0', NULL, '2025-10-03 17:23:58', '2025-10-08 11:08:27');
INSERT INTO `live_room` VALUES (2, 'test', 2, '智慧MOOC新课程安排', 'https://leyen.me/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Flogo.2749a901.png&w=96&q=75', NULL, NULL, NULL, 0, NULL, NULL, 0, 0, 0, 0, b'0', NULL, b'0', NULL, '2025-10-03 18:02:39', '2025-10-08 11:08:31');

-- ----------------------------
-- Table structure for user_follow
-- ----------------------------
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` bigint NOT NULL COMMENT '关注者用户ID',
  `followed_user_id` bigint NOT NULL COMMENT '被关注者用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_followed`(`user_id` ASC, `followed_user_id` ASC) USING BTREE COMMENT '唯一约束，避免重复关注',
  INDEX `idx_followed_user_id`(`followed_user_id` ASC) USING BTREE COMMENT '查询被关注者的粉丝列表'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户关注关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_follow
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
