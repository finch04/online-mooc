/*
 Navicat Premium Dump SQL

 Source Server         : tianji
 Source Server Type    : MySQL
 Source Server Version : 80029 (8.0.29)
 Source Host           : 192.168.150.101:3306
 Source Schema         : tj_pay

 Target Server Type    : MySQL
 Target Server Version : 80029 (8.0.29)
 File Encoding         : 65001

 Date: 26/06/2025 19:49:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for pay_channel
-- ----------------------------
DROP TABLE IF EXISTS `pay_channel`;
CREATE TABLE `pay_channel`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付渠道id',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '支付渠道名称',
  `channel_code` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '支付渠道编码，用于获取支付实现',
  `channel_priority` int NOT NULL COMMENT '渠道优先级，数字越小优先级越高',
  `channel_icon` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '渠道图标',
  `status` int NOT NULL DEFAULT 1 COMMENT '支付渠道状态，1：使用中，2：停用',
  `creater` bigint NOT NULL COMMENT '创建人',
  `updater` bigint NOT NULL COMMENT '更新人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '支付渠道' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_order
-- ----------------------------
DROP TABLE IF EXISTS `pay_order`;
CREATE TABLE `pay_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `biz_order_no` bigint NOT NULL COMMENT '业务订单号',
  `pay_order_no` bigint NOT NULL DEFAULT 0 COMMENT '支付单号',
  `biz_user_id` bigint NOT NULL COMMENT '支付用户id',
  `pay_channel_code` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '支付渠道编码',
  `amount` int NOT NULL COMMENT '支付金额，单位位分',
  `pay_type` tinyint NOT NULL DEFAULT 4 COMMENT '支付类型，1：h5,2:小程序，3：公众号，4：扫码',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态，0：待提交，1:待支付，2：支付超时或取消，3：支付成功',
  `expand_json` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '拓展字段，用于传递不同渠道单独处理的字段',
  `notify_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '业务端回调接口',
  `notify_times` int NOT NULL DEFAULT 0 COMMENT '业务端回调次数',
  `notify_status` int NOT NULL DEFAULT 0 COMMENT '回调状态，0：待回调，1：回调成功，2：回调失败',
  `result_code` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '第三方返回业务码',
  `result_msg` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '第三方返回提示信息',
  `pay_success_time` datetime NULL DEFAULT NULL COMMENT '支付成功时间',
  `pay_over_time` datetime NOT NULL COMMENT '支付超时时间',
  `qr_code_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付二维码链接',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creater` bigint NOT NULL DEFAULT 0 COMMENT '创建人',
  `updater` bigint NOT NULL DEFAULT 0 COMMENT '更新人',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `biz_order_no`(`biz_order_no` ASC) USING BTREE,
  UNIQUE INDEX `pay_order_no`(`pay_order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1934476333766234115 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '支付订单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reconciliation_record
-- ----------------------------
DROP TABLE IF EXISTS `reconciliation_record`;
CREATE TABLE `reconciliation_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `biz_order_no` bigint NULL DEFAULT NULL,
  `pay_order_no` bigint NULL DEFAULT NULL,
  `refund_order_no` bigint NULL DEFAULT NULL,
  `pay_channel_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `amount` int NULL DEFAULT NULL,
  `refund_amount` int NULL DEFAULT NULL,
  `reconciliation_status` int NULL DEFAULT 0,
  `reconciliation_time` datetime NULL DEFAULT NULL,
  `result_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `result_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `creater` bigint NULL DEFAULT NULL,
  `updater` bigint NULL DEFAULT NULL,
  `deleted` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1934221606553952259 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for refund_order
-- ----------------------------
DROP TABLE IF EXISTS `refund_order`;
CREATE TABLE `refund_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_order_no` bigint NOT NULL COMMENT '业务端已支付的订单id',
  `biz_refund_order_no` bigint NOT NULL COMMENT '业务端要退款的订单id，也就是子订单id',
  `pay_order_no` bigint NOT NULL COMMENT '付款时传入的支付单号',
  `refund_order_no` bigint NOT NULL COMMENT '退款单号，每次退款的唯一标示',
  `refund_amount` int NOT NULL COMMENT '本次退款金额，单位分',
  `total_amount` int NOT NULL COMMENT '总金额，单位分',
  `is_split` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否是拆单退款，默认false',
  `pay_channel_code` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '支付渠道编码',
  `result_code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '第三方交易编码',
  `result_msg` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '第三方交易信息',
  `status` int NOT NULL DEFAULT 0 COMMENT '退款状态，0：未提交，1：退款中，2：退款失败，3：退款成功',
  `refund_channel` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '退款渠道',
  `notify_failed_times` int NOT NULL DEFAULT 0 COMMENT '业务端退款通知失败次数',
  `notify_status` int NOT NULL DEFAULT 0 COMMENT '退款接口通知状态，0：待通知，1：通知成功，2：通知中，3：通知失败',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '退款单据创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '退款单据修改时间',
  `creater` bigint NOT NULL DEFAULT 0 COMMENT '单据创建人，一般手动对账产生的单据才有值',
  `updater` bigint NOT NULL DEFAULT 0 COMMENT '单据修改人，一般手动对账产生的单据才有值',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_biz_order_id`(`biz_refund_order_no` ASC) USING BTREE,
  INDEX `index_create_time`(`create_time` ASC) USING BTREE,
  INDEX `index_refund_order_id`(`refund_order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1934541141471469571 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '退款订单' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
