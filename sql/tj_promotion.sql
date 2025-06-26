/*
 Navicat Premium Dump SQL

 Source Server         : tianji
 Source Server Type    : MySQL
 Source Server Version : 80029 (8.0.29)
 Source Host           : 192.168.150.101:3306
 Source Schema         : tj_promotion

 Target Server Type    : MySQL
 Target Server Version : 80029 (8.0.29)
 File Encoding         : 65001

 Date: 26/06/2025 19:49:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for coupon
-- ----------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '优惠券id',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '优惠券名称，可以和活动名称保持一致',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '优惠券类型，1：普通券。目前就一种，保留字段',
  `discount_type` tinyint NOT NULL COMMENT '折扣类型，1：每满减，2：折扣，3：无门槛，4：普通满减',
  `specific` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否限定作用范围，false：不限定，true：限定。默认false',
  `discount_value` int NOT NULL DEFAULT 1 COMMENT '折扣值，如果是满减则存满减金额，如果是折扣，则存折扣率，8折就是存80',
  `threshold_amount` int NOT NULL DEFAULT 0 COMMENT '使用门槛，0：表示无门槛，其他值：最低消费金额',
  `max_discount_amount` int NOT NULL DEFAULT 0 COMMENT '最高优惠金额，满减最大，0：表示没有限制，不为0，则表示该券有金额的限制',
  `obtain_way` tinyint NOT NULL DEFAULT 0 COMMENT '获取方式：1：手动领取，2：兑换码',
  `issue_begin_time` datetime NULL DEFAULT NULL COMMENT '开始发放时间',
  `issue_end_time` datetime NULL DEFAULT NULL COMMENT '结束发放时间',
  `term_days` int NOT NULL DEFAULT 0 COMMENT '优惠券有效期天数，0：表示有效期是指定有效期的',
  `term_begin_time` datetime NULL DEFAULT NULL COMMENT '优惠券有效期开始时间',
  `term_end_time` datetime NULL DEFAULT NULL COMMENT '优惠券有效期结束时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '优惠券配置状态，1：待发放，2：未开始   3：进行中，4：已结束，5：暂停',
  `total_num` int NOT NULL DEFAULT 0 COMMENT '总数量，不超过5000',
  `issue_num` int NOT NULL DEFAULT 0 COMMENT '已发行数量，用于判断是否超发',
  `used_num` int NOT NULL DEFAULT 0 COMMENT '已使用数量',
  `user_limit` int NOT NULL DEFAULT 1 COMMENT '每个人限领的数量，默认1',
  `ext_param` json NULL COMMENT '拓展参数字段，保留字段',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creater` bigint NOT NULL COMMENT '创建人',
  `updater` bigint NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1799677743084540000 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠券的规则信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for coupon_scope
-- ----------------------------
DROP TABLE IF EXISTS `coupon_scope`;
CREATE TABLE `coupon_scope`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` tinyint NOT NULL COMMENT '范围限定类型：1-分类，2-课程，等等',
  `coupon_id` bigint NOT NULL COMMENT '优惠券id',
  `biz_id` bigint NOT NULL COMMENT '优惠券作用范围的业务id，例如分类id、课程id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_coupon`(`coupon_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠券作用范围信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for exchange_code
-- ----------------------------
DROP TABLE IF EXISTS `exchange_code`;
CREATE TABLE `exchange_code`  (
  `id` int NOT NULL COMMENT '兑换码id',
  `code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '兑换码',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '兑换码状态， 1：待兑换，2：已兑换，3：兑换活动已结束',
  `user_id` bigint NOT NULL DEFAULT 0 COMMENT '兑换人',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '兑换类型，1：优惠券，以后再添加其它类型',
  `exchange_target_id` bigint NOT NULL DEFAULT 0 COMMENT '兑换码目标id，例如兑换优惠券，该id则是优惠券的配置id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expired_time` datetime NOT NULL COMMENT '兑换码过期时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_status`(`status` ASC) USING BTREE,
  INDEX `index_config_id`(`exchange_target_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '兑换码' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for promotion
-- ----------------------------
DROP TABLE IF EXISTS `promotion`;
CREATE TABLE `promotion`  (
  `id` bigint NOT NULL COMMENT '促销活动id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动名称',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '促销活动类型：1-优惠券，2-分销',
  `hot` tinyint NOT NULL DEFAULT 0 COMMENT '是否是热门活动：true或false，默认false',
  `begin_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creater` bigint NOT NULL COMMENT '创建人',
  `updater` bigint NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '促销活动，形式多种多样，例如：优惠券' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` bigint NOT NULL,
  `xid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `context` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  `ext` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_undo_log`(`xid` ASC, `branch_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_coupon
-- ----------------------------
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon`  (
  `id` bigint NOT NULL COMMENT '用户券id',
  `user_id` bigint NOT NULL COMMENT '优惠券的拥有者',
  `coupon_id` bigint NOT NULL COMMENT '优惠券模板id',
  `term_begin_time` datetime NULL DEFAULT NULL COMMENT '优惠券有效期开始时间',
  `term_end_time` datetime NOT NULL COMMENT '优惠券有效期结束时间',
  `used_time` datetime NULL DEFAULT NULL COMMENT '优惠券使用时间（核销时间）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '优惠券状态，1：未使用，2：已使用，3：已失效',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_coupon`(`coupon_id` ASC) USING BTREE,
  INDEX `idx_user_coupon`(`user_id` ASC, `coupon_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户领取优惠券的记录，是真正使用的优惠券信息' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
