/*
 Navicat Premium Dump SQL

 Source Server         : tianji
 Source Server Type    : MySQL
 Source Server Version : 80029 (8.0.29)
 Source Host           : 192.168.150.101:3306
 Source Schema         : tj_learning

 Target Server Type    : MySQL
 Target Server Version : 80029 (8.0.29)
 File Encoding         : 65001

 Date: 26/06/2025 19:49:14
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for evaluation
-- ----------------------------
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation`  (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `teacher_id` bigint NULL DEFAULT NULL COMMENT '教师ID（可选）',
  `content_rating` tinyint NOT NULL DEFAULT 0 COMMENT '内容评分(1-5)',
  `teaching_rating` tinyint NOT NULL DEFAULT 0 COMMENT '教学评分(1-5)',
  `difficulty_rating` tinyint NOT NULL DEFAULT 0 COMMENT '难度评分(1-5)',
  `value_rating` tinyint NOT NULL DEFAULT 0 COMMENT '价值评分(1-5)',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `is_anonymous` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否匿名',
  `help_count` int NOT NULL DEFAULT 0 COMMENT '有用次数',
  `hidden` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否被隐藏',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_evaluation`(`id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户课程评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interaction_question
-- ----------------------------
DROP TABLE IF EXISTS `interaction_question`;
CREATE TABLE `interaction_question`  (
  `id` bigint NOT NULL COMMENT '主键，互动问题的id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '互动问题的标题',
  `description` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '问题描述信息',
  `course_id` bigint NOT NULL COMMENT '所属课程id',
  `chapter_id` bigint NOT NULL COMMENT '所属课程章id',
  `section_id` bigint NOT NULL COMMENT '所属课程节id',
  `user_id` bigint NOT NULL COMMENT '提问学员id',
  `latest_answer_id` bigint NULL DEFAULT NULL COMMENT '最新的一个回答的id',
  `answer_times` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '问题下的回答数量',
  `anonymity` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否匿名，默认false',
  `hidden` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否被隐藏，默认false',
  `status` tinyint NULL DEFAULT 0 COMMENT '管理端问题状态：0-未查看，1-已查看',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提问时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `section_id`(`section_id` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '互动提问的问题表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for interaction_reply
-- ----------------------------
DROP TABLE IF EXISTS `interaction_reply`;
CREATE TABLE `interaction_reply`  (
  `id` bigint NOT NULL COMMENT '互动问题的回答id',
  `question_id` bigint NOT NULL COMMENT '互动问题问题id',
  `answer_id` bigint NULL DEFAULT 0 COMMENT '回复的上级回答id',
  `user_id` bigint NOT NULL COMMENT '回答者id',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '回答内容',
  `target_user_id` bigint NULL DEFAULT 0 COMMENT '回复的目标用户id',
  `target_reply_id` bigint NULL DEFAULT 0 COMMENT '回复的目标回复id',
  `reply_times` int NOT NULL DEFAULT 0 COMMENT '评论数量',
  `liked_times` int NOT NULL DEFAULT 0 COMMENT '点赞数量',
  `hidden` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否被隐藏，默认false',
  `anonymity` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否匿名，默认false',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '互动问题的回答或评论' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for learning_lesson
-- ----------------------------
DROP TABLE IF EXISTS `learning_lesson`;
CREATE TABLE `learning_lesson`  (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '学员id',
  `course_id` bigint NOT NULL COMMENT '课程id',
  `status` tinyint NULL DEFAULT 0 COMMENT '课程状态，0-未学习，1-学习中，2-已学完，3-已失效',
  `week_freq` tinyint NULL DEFAULT NULL COMMENT '每周学习频率，例如每周学习6小节，则频率为6',
  `plan_status` tinyint NOT NULL DEFAULT 0 COMMENT '学习计划状态，0-没有计划，1-计划进行中',
  `learned_sections` int NOT NULL DEFAULT 0 COMMENT '已学习小节数量',
  `latest_section_id` bigint NULL DEFAULT NULL COMMENT '最近一次学习的小节id',
  `latest_learn_time` datetime NULL DEFAULT NULL COMMENT '最近一次学习的时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_id`(`user_id` ASC, `course_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生课程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for learning_record
-- ----------------------------
DROP TABLE IF EXISTS `learning_record`;
CREATE TABLE `learning_record`  (
  `id` bigint NOT NULL COMMENT '学习记录的id',
  `lesson_id` bigint NOT NULL COMMENT '对应课表的id',
  `section_id` bigint NOT NULL COMMENT '对应小节的id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `moment` int NULL DEFAULT 0 COMMENT '视频的当前观看时间点，单位秒',
  `finished` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否完成学习，默认false',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '第一次观看时间',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '完成学习的时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（最近一次观看时间）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_lesson_id`(`lesson_id` ASC, `section_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for lesson_collect
-- ----------------------------
DROP TABLE IF EXISTS `lesson_collect`;
CREATE TABLE `lesson_collect`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '学员id',
  `course_id` bigint NOT NULL COMMENT '课程id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_course`(`user_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生课程收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for note
-- ----------------------------
DROP TABLE IF EXISTS `note`;
CREATE TABLE `note`  (
  `id` bigint NOT NULL DEFAULT 0 COMMENT '笔记id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `course_id` bigint NULL DEFAULT NULL COMMENT '课程id',
  `chapter_id` bigint NULL DEFAULT NULL COMMENT '章id',
  `section_id` bigint NULL DEFAULT NULL COMMENT '小节id',
  `note_moment` int NULL DEFAULT NULL COMMENT '记录笔记时的视频播放时间',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '笔记内容',
  `is_private` bit(1) NULL DEFAULT b'0' COMMENT '是否是隐私笔记',
  `gathered_times` int NOT NULL DEFAULT 0 COMMENT '被采集次数',
  `liked_times` int NOT NULL DEFAULT 0 COMMENT '点赞数量',
  `hidden` bit(1) NULL DEFAULT b'0' COMMENT '是否被折叠（隐藏）',
  `hidden_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '被隐藏的原因',
  `author_id` bigint NULL DEFAULT NULL COMMENT '笔记作者id',
  `gathered_note_id` bigint NULL DEFAULT NULL COMMENT '被采集笔记的id',
  `is_gathered` bit(1) NULL DEFAULT b'0' COMMENT '是否是采集他人的笔记',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '笔记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for points_board
-- ----------------------------
DROP TABLE IF EXISTS `points_board`;
CREATE TABLE `points_board`  (
  `id` bigint NOT NULL COMMENT '榜单id',
  `user_id` bigint NOT NULL COMMENT '学生id',
  `points` int NOT NULL COMMENT '积分值',
  `rank` tinyint NOT NULL COMMENT '名次，只记录赛季前100',
  `season` smallint NOT NULL COMMENT '赛季，例如 1,就是第一赛季，2-就是第二赛季',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_season_user`(`season` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学霸天梯榜' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for points_board_season
-- ----------------------------
DROP TABLE IF EXISTS `points_board_season`;
CREATE TABLE `points_board_season`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '自增长id，season标示',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '赛季名称，例如：第1赛季',
  `begin_time` date NOT NULL COMMENT '赛季开始时间',
  `end_time` date NOT NULL COMMENT '赛季结束时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for points_boards_11
-- ----------------------------
DROP TABLE IF EXISTS `points_boards_11`;
CREATE TABLE `points_boards_11`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '榜单id',
  `user_id` bigint NOT NULL COMMENT '学生id',
  `points` int NOT NULL COMMENT '积分值',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学霸天梯榜' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for points_boards_12
-- ----------------------------
DROP TABLE IF EXISTS `points_boards_12`;
CREATE TABLE `points_boards_12`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '榜单id',
  `user_id` bigint NOT NULL COMMENT '学生id',
  `points` int NOT NULL COMMENT '积分值',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学霸天梯榜' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for points_exchange_records
-- ----------------------------
DROP TABLE IF EXISTS `points_exchange_records`;
CREATE TABLE `points_exchange_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '兑换记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `item_id` bigint NOT NULL COMMENT '商品ID',
  `points_used` int NOT NULL COMMENT '使用积分',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '兑换状态：0-待发货，1-已发货，2-已完成，3-已取消',
  `express_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '快递单号',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货地址',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分兑换记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for points_mall_items
-- ----------------------------
DROP TABLE IF EXISTS `points_mall_items`;
CREATE TABLE `points_mall_items`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `item_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `item_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品描述',
  `points_required` int NOT NULL COMMENT '所需积分',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存数量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '商品状态：0-下架，1-上架',
  `image_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片URL',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_points_required`(`points_required` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分商城商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for points_monthly_summary
-- ----------------------------
DROP TABLE IF EXISTS `points_monthly_summary`;
CREATE TABLE `points_monthly_summary`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `year_month` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '年月（YYYYMM）',
  `points_earned` int NOT NULL DEFAULT 0 COMMENT '当月获得积分',
  `points_used` int NOT NULL DEFAULT 0 COMMENT '当月使用积分',
  `points_expired` int NOT NULL DEFAULT 0 COMMENT '当月过期积分',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_month`(`user_id` ASC, `year_month` ASC) USING BTREE,
  INDEX `idx_year_month`(`year_month` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户月度积分汇总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for points_record
-- ----------------------------
DROP TABLE IF EXISTS `points_record`;
CREATE TABLE `points_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '积分记录表id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `type` tinyint NOT NULL COMMENT '积分方式：1-课程学习，2-每日签到，3-课程问答， 4-课程笔记，5-课程评价',
  `points` tinyint NOT NULL COMMENT '积分值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC, `type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 83 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习积分记录，每个月底清零' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
