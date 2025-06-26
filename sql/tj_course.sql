/*
 Navicat Premium Dump SQL

 Source Server         : tianji
 Source Server Type    : MySQL
 Source Server Version : 80029 (8.0.29)
 Source Host           : 192.168.150.101:3306
 Source Schema         : tj_course

 Target Server Type    : MySQL
 Target Server Version : 80029 (8.0.29)
 File Encoding         : 65001

 Date: 26/06/2025 19:48:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程分类id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类id，一级分类父id为0',
  `level` int NOT NULL COMMENT '分类级别，1,2,3：代表一级分类，二级分类，三级分类',
  `priority` int NOT NULL DEFAULT 1 COMMENT '同级目录优先级，数字越小优先级越高，可以重复',
  `status` tinyint NOT NULL COMMENT '课程分类状态，1：正常，2：禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creater` bigint NOT NULL DEFAULT 0 COMMENT '创建者',
  `updater` bigint NOT NULL DEFAULT 0 COMMENT '更新者',
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3656 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` bigint NOT NULL COMMENT '课程草稿id，对应正式草稿id',
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程名称',
  `course_type` tinyint NOT NULL DEFAULT 2 COMMENT '课程类型，1：直播课，2：录播课',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '封面链接',
  `first_cate_id` bigint NOT NULL COMMENT '一级课程分类id',
  `second_cate_id` bigint NOT NULL DEFAULT 0 COMMENT '二级课程分类id',
  `third_cate_id` bigint NOT NULL DEFAULT 0 COMMENT '三级课程分类id',
  `free` tinyint NOT NULL DEFAULT 0 COMMENT '售卖方式0付费，1：免费',
  `price` int NOT NULL COMMENT '课程价格，单位为分',
  `template_type` tinyint NOT NULL DEFAULT 1 COMMENT '模板类型，1：固定模板，2：自定义模板',
  `template_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '自定义模板的连接',
  `status` tinyint NOT NULL COMMENT '课程状态，1：待上架，2：已上架，3：下架，4：已完结',
  `purchase_start_time` datetime NULL DEFAULT NULL COMMENT '课程购买有效期开始时间',
  `purchase_end_time` datetime NOT NULL COMMENT '课程购买有效期结束时间',
  `step` tinyint NOT NULL COMMENT '信息填写进度',
  `score` int NULL DEFAULT 0 COMMENT '课程评价得分，45代表4.5星',
  `media_duration` int(10) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '课程总时长',
  `valid_duration` int NOT NULL COMMENT '课程有效期，单位月',
  `section_num` int NULL DEFAULT NULL COMMENT '课程总节数，包括练习',
  `dep_id` bigint NOT NULL COMMENT '部门id',
  `publish_times` int NULL DEFAULT 1 COMMENT '发布次数',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '最近一次发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creater` bigint NOT NULL COMMENT '创建人',
  `updater` bigint NOT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '草稿课程' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_cata_subject_draft
-- ----------------------------
DROP TABLE IF EXISTS `course_cata_subject_draft`;
CREATE TABLE `course_cata_subject_draft`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '小节题目关系id',
  `course_id` bigint NULL DEFAULT NULL,
  `cata_id` bigint NOT NULL COMMENT '小节id',
  `subject_id` bigint NOT NULL COMMENT '题目id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 244 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程-题目关系表草稿' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_catalogue
-- ----------------------------
DROP TABLE IF EXISTS `course_catalogue`;
CREATE TABLE `course_catalogue`  (
  `id` bigint NOT NULL COMMENT '课程目录id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目录名称',
  `trailer` tinyint NOT NULL DEFAULT 0 COMMENT '是否支持试看',
  `course_id` bigint NOT NULL COMMENT '课程id',
  `type` tinyint NOT NULL COMMENT '目录类型1：章，2：节，3：测试',
  `parent_catalogue_id` bigint NOT NULL DEFAULT 0 COMMENT '所属章id，只有小节和测试有该值，章没有，章默认为0',
  `media_id` bigint NOT NULL DEFAULT 0 COMMENT '媒资id',
  `video_id` bigint NULL DEFAULT NULL COMMENT '视频id',
  `video_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '视频名称',
  `living_start_time` datetime NULL DEFAULT NULL COMMENT '直播开始时间',
  `living_end_time` datetime NULL DEFAULT NULL COMMENT '直播结束时间',
  `play_back` tinyint NOT NULL DEFAULT 0 COMMENT '是否支持回放',
  `media_duration` int NOT NULL DEFAULT 0 COMMENT '视频时长，以秒为单位',
  `c_index` int NOT NULL DEFAULT 0 COMMENT '用于章节排序',
  `dep_id` bigint NOT NULL COMMENT '部门id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `creater` bigint NOT NULL DEFAULT 0 COMMENT '创建人',
  `updater` bigint NOT NULL DEFAULT 0 COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '目录草稿' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_catalogue_draft
-- ----------------------------
DROP TABLE IF EXISTS `course_catalogue_draft`;
CREATE TABLE `course_catalogue_draft`  (
  `id` bigint NOT NULL COMMENT '课程目录id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目录名称',
  `trailer` tinyint NOT NULL DEFAULT 0 COMMENT '是否支持试看',
  `course_id` bigint NOT NULL COMMENT '课程id',
  `type` tinyint NOT NULL COMMENT '目录类型1：章节，2：小节，3：测试',
  `parent_catalogue_id` bigint NOT NULL DEFAULT 0 COMMENT '所属章节id，只有小节和测试有该值，章节没有，章节默认为0',
  `media_id` bigint NOT NULL DEFAULT 0 COMMENT '媒资id',
  `video_id` bigint NULL DEFAULT NULL COMMENT '视频id',
  `video_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '视频名称',
  `living_start_time` datetime NULL DEFAULT NULL COMMENT '直播开始时间',
  `living_end_time` datetime NULL DEFAULT NULL COMMENT '直播结束时间',
  `play_back` tinyint NOT NULL DEFAULT 0 COMMENT '是否支持回放',
  `c_index` int NOT NULL DEFAULT 0 COMMENT '用于章节排序',
  `media_duration` int NOT NULL DEFAULT 0 COMMENT '以s为单位',
  `can_update` tinyint NOT NULL DEFAULT 1 COMMENT '是否可以更新0：不可以更新，1：可以更新',
  `dep_id` bigint NOT NULL DEFAULT 0 COMMENT '部门id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creater` bigint NOT NULL DEFAULT 0 COMMENT '创建人',
  `updater` bigint NOT NULL DEFAULT 0 COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '目录草稿' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_content
-- ----------------------------
DROP TABLE IF EXISTS `course_content`;
CREATE TABLE `course_content`  (
  `id` bigint NOT NULL COMMENT '课程内容id',
  `course_introduce` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程介绍',
  `use_people` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '适用人群',
  `course_detail` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程详情',
  `dep_id` bigint NOT NULL DEFAULT 0 COMMENT '部门id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creater` bigint NOT NULL COMMENT '创建人',
  `updater` bigint NOT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程内容，主要是一些大文本' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_content_draft
-- ----------------------------
DROP TABLE IF EXISTS `course_content_draft`;
CREATE TABLE `course_content_draft`  (
  `id` bigint NOT NULL COMMENT '课程内容id',
  `course_introduce` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程介绍',
  `use_people` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '适用人群',
  `course_detail` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程详情',
  `dep_id` bigint NOT NULL DEFAULT 0 COMMENT '部门id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creater` bigint NOT NULL DEFAULT 0 COMMENT '创建人',
  `updater` bigint NOT NULL DEFAULT 0 COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程内容，主要是一些大文本' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_draft
-- ----------------------------
DROP TABLE IF EXISTS `course_draft`;
CREATE TABLE `course_draft`  (
  `id` bigint NOT NULL COMMENT '课程草稿id，对应正式草稿id',
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程名称',
  `course_type` tinyint NOT NULL DEFAULT 2 COMMENT '课程类型，1：直播课，2：录播课',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '封面链接',
  `first_cate_id` bigint NOT NULL COMMENT '一级课程分类id',
  `second_cate_id` bigint NOT NULL DEFAULT 0 COMMENT '二级课程分类id',
  `third_cate_id` bigint NOT NULL DEFAULT 0 COMMENT '三级课程分类id',
  `free` tinyint NOT NULL DEFAULT 0 COMMENT '售卖方式0付费，1：免费',
  `price` int NOT NULL COMMENT '课程价格，单位为分',
  `template_type` tinyint NOT NULL DEFAULT 1 COMMENT '模板类型，1：固定模板，2：自定义模板',
  `template_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '自定义模板的连接',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '课程状态，0：待上架，1：已上架，2：下架，3：已完结',
  `purchase_start_time` datetime NULL DEFAULT NULL COMMENT '课程购买有效期开始时间',
  `purchase_end_time` datetime NOT NULL COMMENT '课程购买有效期结束时间',
  `step` tinyint NOT NULL COMMENT '信息填写进度1：基本信息已经保存，2：课程目录已经保存，3：课程视频已保存，4：课程题目已保存，5：课程老师已经保存',
  `score` int NULL DEFAULT 0 COMMENT '课程评价得分，45代表4.5星',
  `media_duration` int NOT NULL DEFAULT 0 COMMENT '视频总时长',
  `valid_duration` int NOT NULL DEFAULT 0 COMMENT '课程有效期，单位月',
  `section_num` int NOT NULL DEFAULT 0 COMMENT '课程总节数',
  `can_update` tinyint NOT NULL DEFAULT 1 COMMENT '是否可以更新',
  `dep_id` bigint NOT NULL DEFAULT 0 COMMENT '部门id',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '最近一次发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creater` bigint NOT NULL DEFAULT 0 COMMENT '创建人',
  `updater` bigint NOT NULL DEFAULT 0 COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `c_version` int NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '草稿课程' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_subject
-- ----------------------------
DROP TABLE IF EXISTS `course_subject`;
CREATE TABLE `course_subject`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程题目关系id',
  `course_id` bigint NOT NULL,
  `subject_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程题目关系列表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_teacher
-- ----------------------------
DROP TABLE IF EXISTS `course_teacher`;
CREATE TABLE `course_teacher`  (
  `id` bigint NOT NULL COMMENT '课程老师关系id',
  `course_id` bigint NOT NULL COMMENT '课程id',
  `teacher_id` bigint NOT NULL COMMENT '老师id',
  `is_show` tinyint NOT NULL DEFAULT 0 COMMENT '用户端是否展示',
  `c_index` int NOT NULL COMMENT '序号',
  `dep_id` bigint NOT NULL COMMENT '部门id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `creater` bigint NOT NULL COMMENT '创建人',
  `updater` bigint NOT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程老师关系表草稿' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_teacher_draft
-- ----------------------------
DROP TABLE IF EXISTS `course_teacher_draft`;
CREATE TABLE `course_teacher_draft`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程老师关系id',
  `course_id` bigint NOT NULL COMMENT '课程id',
  `teacher_id` bigint NOT NULL COMMENT '老师id',
  `is_show` tinyint NOT NULL DEFAULT 0 COMMENT '用户端是否展示',
  `c_index` int NOT NULL COMMENT '序号',
  `dep_id` bigint NOT NULL DEFAULT 0 COMMENT '部门id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creater` bigint NOT NULL DEFAULT 0 COMMENT '创建人',
  `updater` bigint NOT NULL DEFAULT 0 COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 158 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程老师关系表草稿' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for subject
-- ----------------------------
DROP TABLE IF EXISTS `subject`;
CREATE TABLE `subject`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目id',
  `name` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题干',
  `subject_type` tinyint NOT NULL COMMENT '题目类型，1：单选题，2：多选题，3：不定向选择题，4：判断题，5：主观题',
  `difficulty` tinyint NOT NULL COMMENT '难易度，1：简单，2：中等，3：困难',
  `option1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '选择题答案1，',
  `option2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '选择题答案2',
  `option3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '选择题答案3',
  `option4` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '选择题答案4',
  `option5` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '选择题答案5',
  `option6` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '选择题答案6',
  `option7` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '选择题答案7',
  `option8` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '选择题答案8',
  `option9` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '选择题答案9',
  `option10` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '选择题答案10',
  `answer` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选择题正确答案1到10，如果有多个答案，中间使用逗号隔开，如果是判断题，1：代表正确，其他代表错误',
  `analysis` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '答案解析',
  `use_times` int NOT NULL DEFAULT 0 COMMENT '引用次数',
  `answer_times` int NOT NULL DEFAULT 0 COMMENT '回答次数',
  `score` int NOT NULL COMMENT '分值',
  `dep_id` bigint NULL DEFAULT NULL COMMENT '部门id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creater` bigint NOT NULL COMMENT '创建人',
  `updater` bigint NOT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 70 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题目' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for subject_category
-- ----------------------------
DROP TABLE IF EXISTS `subject_category`;
CREATE TABLE `subject_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `subject_id` bigint NOT NULL COMMENT '题目id',
  `first_cate_id` bigint NOT NULL COMMENT '一级课程分类id',
  `second_cate_id` bigint NULL DEFAULT NULL COMMENT '二级课程分类id',
  `third_cate_id` bigint NULL DEFAULT NULL COMMENT '三级课程分类id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5334 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程分类关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log`  (
  `branch_id` bigint NOT NULL COMMENT 'branch transaction id',
  `xid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'global transaction id',
  `context` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` longblob NOT NULL COMMENT 'rollback info',
  `log_status` int NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` datetime(6) NOT NULL COMMENT 'create datetime',
  `log_modified` datetime(6) NOT NULL COMMENT 'modify datetime',
  UNIQUE INDEX `ux_undo_log`(`xid` ASC, `branch_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'AT transaction mode undo table' ROW_FORMAT = COMPACT;

SET FOREIGN_KEY_CHECKS = 1;
