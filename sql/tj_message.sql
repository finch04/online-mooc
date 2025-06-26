/*
 Navicat Premium Dump SQL

 Source Server         : tianji
 Source Server Type    : MySQL
 Source Server Version : 80029 (8.0.29)
 Source Host           : 192.168.150.101:3306
 Source Schema         : tj_message

 Target Server Type    : MySQL
 Target Server Version : 80029 (8.0.29)
 File Encoding         : 65001

 Date: 26/06/2025 19:49:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_groups
-- ----------------------------
DROP TABLE IF EXISTS `chat_groups`;
CREATE TABLE `chat_groups`  (
  `id` bigint NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 0,
  `creator_id` bigint NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_creator`(`creator_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `message_type` tinyint NOT NULL COMMENT '1:私聊 2:群聊',
  `target_id` bigint NOT NULL COMMENT '接收者ID或群ID',
  `sent_at` datetime(3) NOT NULL COMMENT '精确到毫秒',
  `status` tinyint NULL DEFAULT 1 COMMENT '1:已发送 2:已接收 3:已读',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender`(`sender_id` ASC) USING BTREE,
  INDEX `idx_target_type`(`target_id` ASC, `message_type` ASC) USING BTREE,
  INDEX `idx_sent`(`sent_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = COMPRESSED;

-- ----------------------------
-- Table structure for group_members
-- ----------------------------
DROP TABLE IF EXISTS `group_members`;
CREATE TABLE `group_members`  (
  `id` bigint NOT NULL,
  `group_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `joined_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_user`(`group_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_receipts
-- ----------------------------
DROP TABLE IF EXISTS `message_receipts`;
CREATE TABLE `message_receipts`  (
  `id` bigint NOT NULL,
  `message_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `status` tinyint NULL DEFAULT 1 COMMENT '1:已发送 2:已接收 3:已读',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user`(`message_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_template
-- ----------------------------
DROP TABLE IF EXISTS `message_template`;
CREATE TABLE `message_template`  (
  `id` bigint NOT NULL COMMENT '短信发送模板id',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '模板名称',
  `platform_code` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '第三方短信平台代号',
  `sign_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '签名',
  `third_template_code` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '第三方短信模板code',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '第三方短信模板内容预览',
  `template_id` bigint NOT NULL COMMENT '通知模板id',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '模板状态:  0-禁用，1-启用',
  `creater` bigint NOT NULL COMMENT '创建者',
  `updater` bigint NOT NULL COMMENT '更新者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_template_id`(`template_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '第三方短信平台签名和模板信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notice_task
-- ----------------------------
DROP TABLE IF EXISTS `notice_task`;
CREATE TABLE `notice_task`  (
  `id` bigint NOT NULL COMMENT '公告任务id',
  `template_id` bigint NOT NULL COMMENT '任务对应的通知模板id',
  `name` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '任务名称',
  `partial` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否是部分人的通告，默认false',
  `push_time` datetime NULL DEFAULT NULL COMMENT '任务预期执行时间',
  `interval` int NULL DEFAULT NULL COMMENT '任务延迟执行时间间隔，单位是分钟',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '任务失效时间',
  `max_times` int NULL DEFAULT 1 COMMENT '任务重复执行次数上限，1则只发一次',
  `finished` bit(1) NULL DEFAULT b'0' COMMENT '任务是否完成，默认false',
  `creater` bigint NOT NULL COMMENT '创建人',
  `updater` bigint NOT NULL COMMENT '更新人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '系统通告的任务表，可以延期或定期发送通告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notice_task_target
-- ----------------------------
DROP TABLE IF EXISTS `notice_task_target`;
CREATE TABLE `notice_task_target`  (
  `task_id` bigint NOT NULL COMMENT '任务id',
  `target_id` bigint NOT NULL COMMENT '目标用户id',
  PRIMARY KEY (`task_id`, `target_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知任务的目标用户信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notice_template
-- ----------------------------
DROP TABLE IF EXISTS `notice_template`;
CREATE TABLE `notice_template`  (
  `id` bigint NOT NULL COMMENT '通知模板id',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '通知模板名称',
  `code` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '模板代号，例如：verify-code',
  `type` tinyint NOT NULL COMMENT '通知类型：0-系统通知，1-笔记通知，2-问答通知，3-其它通知',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '模板状态:  0-草稿，1-使用中，2-停用',
  `title` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '通知标题，短信模板可以不填',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '通知内容模板',
  `is_sms_template` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否是短信模板，默认false',
  `creater` bigint NOT NULL COMMENT '创建人',
  `updater` bigint NOT NULL COMMENT '更新人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '通知模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for public_notice
-- ----------------------------
DROP TABLE IF EXISTS `public_notice`;
CREATE TABLE `public_notice`  (
  `id` bigint NOT NULL COMMENT '公告id',
  `title` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '公告标题',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '公告通知内容，可以存放公告消息模板',
  `type` tinyint NOT NULL COMMENT '通知类型：0-系统通知，1-笔记通知，2-问答通知，3-其它通知',
  `push_time` datetime NOT NULL COMMENT '公告预期发送时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '通知发布时间',
  `expire_time` datetime NOT NULL COMMENT '通知失效时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '公告消息模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sensitive
-- ----------------------------
DROP TABLE IF EXISTS `sensitive`;
CREATE TABLE `sensitive`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sensitives` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '敏感词',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1933865522844696588 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '敏感词信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sms_third_platform
-- ----------------------------
DROP TABLE IF EXISTS `sms_third_platform`;
CREATE TABLE `sms_third_platform`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '短信平台id',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '短信平台名称',
  `code` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '短信平台代码，例如：ali',
  `priority` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '数字越小优先级越高，最小为0',
  `status` int NOT NULL DEFAULT 1 COMMENT '短信平台状态：0-禁用，1-启用',
  `creater` bigint NOT NULL COMMENT '创建人',
  `updater` bigint NOT NULL COMMENT '更新人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '第三方云通讯平台' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_conversation
-- ----------------------------
DROP TABLE IF EXISTS `user_conversation`;
CREATE TABLE `user_conversation`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '会话ID',
  `user_id_1` bigint UNSIGNED NOT NULL COMMENT '参与用户1',
  `user_id_2` bigint UNSIGNED NOT NULL COMMENT '参与用户2',
  `last_message_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '最后一条消息ID',
  `unread_count_1` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户1的未读数量',
  `unread_count_2` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户2的未读数量',
  `last_update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '会话状态（0=正常，1=已屏蔽）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_users`(`user_id_1` ASC, `user_id_2` ASC) USING BTREE,
  INDEX `idx_user1`(`user_id_1` ASC, `last_update_time` ASC) USING BTREE,
  INDEX `idx_user2`(`user_id_2` ASC, `last_update_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_inbox
-- ----------------------------
DROP TABLE IF EXISTS `user_inbox`;
CREATE TABLE `user_inbox`  (
  `id` bigint NOT NULL COMMENT '用户通知id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `notice_id` bigint NULL DEFAULT NULL COMMENT '公告id，如果是系统通知，则会存入公告id，防止重发',
  `type` tinyint NULL DEFAULT 4 COMMENT '通知类型：0-系统通知，1-笔记通知，2-问答通知，3-其它通知',
  `title` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '通知标题',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '通知内容',
  `is_read` bit(1) NOT NULL DEFAULT b'0' COMMENT '公告是否已读',
  `publisher` bigint NOT NULL DEFAULT 0 COMMENT '通知的发送者id，0则代表是系统',
  `push_time` datetime NOT NULL COMMENT '创建时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间，一旦过期用户端不在展示',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `push_time`(`push_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户通知记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_private_message
-- ----------------------------
DROP TABLE IF EXISTS `user_private_message`;
CREATE TABLE `user_private_message`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '消息ID',
  `sender_id` bigint UNSIGNED NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint UNSIGNED NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `message_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '消息类型（0=文本，1=图片，2=语音，3=文件）',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '已读状态（0=未读，1=已读）',
  `push_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '消息状态（0=正常消息，1=置顶消息）',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记（0=未删除，1=已删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender_receiver`(`sender_id` ASC, `receiver_id` ASC, `delete_flag` ASC) USING BTREE,
  INDEX `idx_session_time`(`push_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户私信表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
