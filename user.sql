/*
 Navicat Premium Dump SQL

 Source Server         : 金山生产11
 Source Server Type    : MySQL
 Source Server Version : 50741 (5.7.41-log)
 Source Host           : 10.152.160.27:62112
 Source Schema         : wps_plus_core

 Target Server Type    : MySQL
 Target Server Version : 50741 (5.7.41-log)
 File Encoding         : 65001

 Date: 06/05/2026 23:02:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `comp_uid` bigint(20) NOT NULL COMMENT '企业ID',
  `userid` bigint(20) NOT NULL COMMENT '成员真实ID',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像',
  `comp_id` bigint(20) NOT NULL COMMENT '企业ID',
  `def_dept_id` bigint(20) NOT NULL COMMENT '默认部门ID',
  `user_name` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名称',
  `phone` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  `account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  `login_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '用户登录名',
  `email` varchar(511) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  `status` enum('active','notactive','dimission','disabled') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户状态',
  `roleid` bigint(20) NOT NULL COMMENT '角色ID',
  `dept_num` int(11) NULL DEFAULT 0 COMMENT '用户部门数量',
  `is_hide` tinyint(1) NULL DEFAULT 0,
  `employee_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `telephone` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `ctime` int(11) NOT NULL,
  `mtime` int(11) NOT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '',
  `atime` int(11) NULL DEFAULT 0,
  `employer` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '就职单位',
  `gender` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '性别',
  `country` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '国家或地区',
  `city` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '城市',
  `work_place` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '办公地点',
  `leader` bigint(20) NOT NULL DEFAULT 0 COMMENT '直属主管',
  `employment_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '员工类型',
  `employment_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '员工状态',
  `platform_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '第三方平台id',
  `third_union_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '第三方平台union_id',
  `alias_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '别名',
  PRIMARY KEY (`comp_uid`) USING BTREE,
  INDEX `_index_account`(`account`(191)) USING BTREE,
  INDEX `_index_userid`(`userid`) USING BTREE,
  INDEX `_index_comp_id`(`comp_id`) USING BTREE,
  INDEX `idx_cid_account`(`comp_id`, `account`(191)) USING BTREE,
  INDEX `idx_cid_email`(`comp_id`, `email`(191)) USING BTREE,
  INDEX `idx_cid_phone`(`comp_id`, `phone`) USING BTREE,
  INDEX `idx_cid_cuid`(`comp_id`, `comp_uid`) USING BTREE,
  INDEX `idx_cuid_status`(`comp_uid`, `status`) USING BTREE,
  INDEX `idx_c_p_t_id`(`comp_id`, `platform_id`, `third_union_id`) USING BTREE,
  INDEX `idx_p_t_id`(`platform_id`, `third_union_id`) USING BTREE,
  INDEX `idx_cid_l_name`(`comp_id`, `login_name`) USING BTREE,
  INDEX `idx_cid_status`(`comp_id`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
