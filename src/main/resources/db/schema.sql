-- =====================================================
-- WPS文档操作日志统计分析 - 数据库建表脚本
-- 数据库：MySQL 8.0
-- =====================================================

CREATE DATABASE IF NOT EXISTS `wps_plus_core`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `wps_plus_core`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 用户表
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `comp_uid`          bigint       NOT NULL COMMENT '企业用户ID（主键）',
    `userid`            bigint       NOT NULL COMMENT '成员真实ID（关联日志表operator_id）',
    `avatar`            varchar(255) NULL     DEFAULT NULL COMMENT '头像',
    `comp_id`           bigint       NOT NULL COMMENT '企业ID',
    `def_dept_id`       bigint       NOT NULL COMMENT '默认部门ID',
    `user_name`         varchar(127) NOT NULL COMMENT '用户名称',
    `phone`             varchar(127) NOT NULL DEFAULT '' COMMENT '手机号',
    `account`           varchar(255) NOT NULL DEFAULT '' COMMENT '账号',
    `login_name`        varchar(100) NOT NULL DEFAULT '' COMMENT '用户登录名（关联CSV loginname）',
    `email`             varchar(511) NOT NULL DEFAULT '' COMMENT '邮箱',
    `status`            enum ('active','notactive','dimission','disabled')
                                     NOT NULL COMMENT '用户状态',
    `roleid`            bigint       NOT NULL COMMENT '角色ID',
    `dept_num`          int          NULL     DEFAULT 0 COMMENT '用户部门数量',
    `is_hide`           tinyint(1)   NULL     DEFAULT 0 COMMENT '是否隐藏',
    `employee_id`       varchar(50)  NULL     DEFAULT NULL COMMENT '员工编号',
    `telephone`         varchar(127) NOT NULL DEFAULT '' COMMENT '座机号',
    `title`             varchar(255) NOT NULL DEFAULT '' COMMENT '职位',
    `source`            varchar(50)  NULL     DEFAULT NULL COMMENT '来源',
    `ctime`             int          NOT NULL COMMENT '创建时间（Unix秒时间戳）',
    `mtime`             int          NOT NULL COMMENT '修改时间（Unix秒时间戳）',
    `address`           varchar(255) NULL     DEFAULT '' COMMENT '地址',
    `atime`             int          NULL     DEFAULT 0 COMMENT '最后活跃时间',
    `employer`          varchar(127) NOT NULL DEFAULT '' COMMENT '就职单位',
    `gender`            varchar(30)  NOT NULL DEFAULT '' COMMENT '性别',
    `country`           varchar(255) NOT NULL DEFAULT '' COMMENT '国家或地区',
    `city`              varchar(255) NOT NULL DEFAULT '' COMMENT '城市',
    `work_place`        varchar(255) NOT NULL DEFAULT '' COMMENT '办公地点',
    `leader`            bigint       NOT NULL DEFAULT 0 COMMENT '直属主管userid',
    `employment_type`   varchar(30)  NOT NULL DEFAULT '' COMMENT '员工类型',
    `employment_status` varchar(30)  NOT NULL DEFAULT '' COMMENT '员工状态',
    `platform_id`       varchar(100) NOT NULL DEFAULT '' COMMENT '第三方平台ID',
    `third_union_id`    varchar(100) NOT NULL DEFAULT '' COMMENT '第三方平台union_id',
    `alias_name`        varchar(30)  NOT NULL DEFAULT '' COMMENT '别名',
    PRIMARY KEY (`comp_uid`) USING BTREE,
    INDEX `_index_account` (`account` (191)) USING BTREE,
    INDEX `_index_userid` (`userid`) USING BTREE,
    INDEX `_index_comp_id` (`comp_id`) USING BTREE,
    INDEX `idx_cid_account` (`comp_id`, `account` (191)) USING BTREE,
    INDEX `idx_cid_email` (`comp_id`, `email` (191)) USING BTREE,
    INDEX `idx_cid_phone` (`comp_id`, `phone`) USING BTREE,
    INDEX `idx_cid_cuid` (`comp_id`, `comp_uid`) USING BTREE,
    INDEX `idx_cuid_status` (`comp_uid`, `status`) USING BTREE,
    INDEX `idx_c_p_t_id` (`comp_id`, `platform_id`, `third_union_id`) USING BTREE,
    INDEX `idx_p_t_id` (`platform_id`, `third_union_id`) USING BTREE,
    INDEX `idx_cid_l_name` (`comp_id`, `login_name`) USING BTREE,
    INDEX `idx_cid_status` (`comp_id`, `status`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = Dynamic
    COMMENT = '用户表';

-- =====================================================
-- 文档操作日志分表模板说明
-- 按自然月分表，表名规则：log_doc_yyyy_MM（如 log_doc_2025_06）
-- =====================================================

-- 示例分表：log_doc_2025_01
DROP TABLE IF EXISTS `log_doc_2025_01`;
CREATE TABLE `log_doc_2025_01`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `comp_id`        bigint       NOT NULL COMMENT '企业ID',
    `ext_id`         bigint       NOT NULL COMMENT '扩展ID',
    `dept_id_path`   varchar(1000) NOT NULL COMMENT '部门ID路径',
    `file_name`      varchar(511) NOT NULL COMMENT '文件名',
    `group_id`       bigint       NOT NULL COMMENT '分组ID',
    `ip_addr`        varchar(50)  NOT NULL COMMENT 'IP地址',
    `meta_data`      text         NOT NULL COMMENT '元数据',
    `operation_time` bigint       NOT NULL COMMENT '操作时间（Unix毫秒时间戳）',
    `operation_type` varchar(255) NOT NULL COMMENT '操作类型',
    `operator_email` varchar(255) NOT NULL COMMENT '操作人邮箱',
    `operator_id`    bigint       NOT NULL COMMENT '操作人ID（关联用户表userid）',
    `operator_name`  varchar(127) NOT NULL COMMENT '操作人名称',
    `platform_type`  varchar(20)  NOT NULL COMMENT '平台类型',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_c` (`comp_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = Dynamic
    COMMENT = '文档操作日志-2025年1月';

-- 示例分表：log_doc_2025_02
DROP TABLE IF EXISTS `log_doc_2025_02`;
CREATE TABLE `log_doc_2025_02`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `comp_id`        bigint       NOT NULL COMMENT '企业ID',
    `ext_id`         bigint       NOT NULL COMMENT '扩展ID',
    `dept_id_path`   varchar(1000) NOT NULL COMMENT '部门ID路径',
    `file_name`      varchar(511) NOT NULL COMMENT '文件名',
    `group_id`       bigint       NOT NULL COMMENT '分组ID',
    `ip_addr`        varchar(50)  NOT NULL COMMENT 'IP地址',
    `meta_data`      text         NOT NULL COMMENT '元数据',
    `operation_time` bigint       NOT NULL COMMENT '操作时间（Unix毫秒时间戳）',
    `operation_type` varchar(255) NOT NULL COMMENT '操作类型',
    `operator_email` varchar(255) NOT NULL COMMENT '操作人邮箱',
    `operator_id`    bigint       NOT NULL COMMENT '操作人ID（关联用户表userid）',
    `operator_name`  varchar(127) NOT NULL COMMENT '操作人名称',
    `platform_type`  varchar(20)  NOT NULL COMMENT '平台类型',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_c` (`comp_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = Dynamic
    COMMENT = '文档操作日志-2025年2月';

-- 示例分表：log_doc_2025_03
DROP TABLE IF EXISTS `log_doc_2025_03`;
CREATE TABLE `log_doc_2025_03`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `comp_id`        bigint       NOT NULL COMMENT '企业ID',
    `ext_id`         bigint       NOT NULL COMMENT '扩展ID',
    `dept_id_path`   varchar(1000) NOT NULL COMMENT '部门ID路径',
    `file_name`      varchar(511) NOT NULL COMMENT '文件名',
    `group_id`       bigint       NOT NULL COMMENT '分组ID',
    `ip_addr`        varchar(50)  NOT NULL COMMENT 'IP地址',
    `meta_data`      text         NOT NULL COMMENT '元数据',
    `operation_time` bigint       NOT NULL COMMENT '操作时间（Unix毫秒时间戳）',
    `operation_type` varchar(255) NOT NULL COMMENT '操作类型',
    `operator_email` varchar(255) NOT NULL COMMENT '操作人邮箱',
    `operator_id`    bigint       NOT NULL COMMENT '操作人ID（关联用户表userid）',
    `operator_name`  varchar(127) NOT NULL COMMENT '操作人名称',
    `platform_type`  varchar(20)  NOT NULL COMMENT '平台类型',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_c` (`comp_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = Dynamic
    COMMENT = '文档操作日志-2025年3月';

-- =====================================================
-- 插入示例用户数据
-- =====================================================
INSERT INTO `user` (`comp_uid`, `userid`, `comp_id`, `def_dept_id`, `user_name`, `login_name`,
                    `account`, `email`, `phone`, `status`, `roleid`, `ctime`, `mtime`,
                    `employer`, `gender`, `country`, `city`, `work_place`, `leader`,
                    `employment_type`, `employment_status`, `platform_id`, `third_union_id`,
                    `alias_name`, `telephone`, `title`)
VALUES (1001, 10001, 100, 1, '张三', 'zhangsan', 'zhangsan@example.com', 'zhangsan@example.com',
        '13800000001', 'active', 1, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(),
        '', '', '', '', '', 0, '', '', '', '', '', '', ''),
       (1002, 10002, 100, 1, '李四', 'lisi', 'lisi@example.com', 'lisi@example.com',
        '13800000002', 'active', 1, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(),
        '', '', '', '', '', 0, '', '', '', '', '', '', ''),
       (1003, 10003, 100, 2, '王五', 'wangwu', 'wangwu@example.com', 'wangwu@example.com',
        '13800000003', 'active', 1, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(),
        '', '', '', '', '', 0, '', '', '', '', '', '', ''),
       (1004, 10004, 100, 2, '赵六', 'zhaoliu', 'zhaoliu@example.com', 'zhaoliu@example.com',
        '13800000004', 'active', 1, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(),
        '', '', '', '', '', 0, '', '', '', '', '', '', ''),
       (1005, 10005, 100, 3, '孙七', 'sunqi', 'sunqi@example.com', 'sunqi@example.com',
        '13800000005', 'active', 1, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(),
        '', '', '', '', '', 0, '', '', '', '', '', '', ''),
       (1006, 10006, 100, 3, '周八', 'zhouba', 'zhouba@example.com', 'zhouba@example.com',
        '13800000006', 'active', 1, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(),
        '', '', '', '', '', 0, '', '', '', '', '', '', '');

-- =====================================================
-- 插入示例日志数据
-- =====================================================

-- 2025年1月日志
INSERT INTO `log_doc_2025_01` (`comp_id`, `ext_id`, `dept_id_path`, `file_name`, `group_id`,
                                `ip_addr`, `meta_data`, `operation_time`, `operation_type`,
                                `operator_email`, `operator_id`, `operator_name`, `platform_type`)
VALUES (100, 1, '/1', '测试文档1.docx', 1, '127.0.0.1', '{}',
        UNIX_TIMESTAMP('2025-01-15 10:00:00') * 1000, 'VIEW',
        'zhangsan@example.com', 10001, '张三', 'PC'),
       (100, 2, '/1', '测试文档2.docx', 1, '127.0.0.1', '{}',
        UNIX_TIMESTAMP('2025-01-16 11:00:00') * 1000, 'EDIT',
        'lisi@example.com', 10002, '李四', 'PC'),
       (100, 3, '/2', '测试文档3.docx', 2, '127.0.0.1', '{}',
        UNIX_TIMESTAMP('2025-01-17 12:00:00') * 1000, 'DOWNLOAD',
        'wangwu@example.com', 10003, '王五', 'MOBILE'),
       (100, 4, '/2', '测试文档4.docx', 2, '127.0.0.1', '{}',
        UNIX_TIMESTAMP('2025-01-18 13:00:00') * 1000, 'VIEW',
        'zhaoliu@example.com', 10004, '赵六', 'PC'),
       (100, 1, '/1', '测试文档1.docx', 1, '127.0.0.1', '{}',
        UNIX_TIMESTAMP('2025-01-20 14:00:00') * 1000, 'VIEW',
        'zhangsan@example.com', 10001, '张三', 'PC');

-- 2025年2月日志
INSERT INTO `log_doc_2025_02` (`comp_id`, `ext_id`, `dept_id_path`, `file_name`, `group_id`,
                                `ip_addr`, `meta_data`, `operation_time`, `operation_type`,
                                `operator_email`, `operator_id`, `operator_name`, `platform_type`)
VALUES (100, 1, '/1', '测试文档A.docx', 1, '127.0.0.1', '{}',
        UNIX_TIMESTAMP('2025-02-10 10:00:00') * 1000, 'VIEW',
        'zhangsan@example.com', 10001, '张三', 'PC'),
       (100, 2, '/3', '测试文档B.docx', 3, '127.0.0.1', '{}',
        UNIX_TIMESTAMP('2025-02-12 11:00:00') * 1000, 'EDIT',
        'sunqi@example.com', 10005, '孙七', 'PC'),
       (100, 3, '/3', '测试文档C.docx', 3, '127.0.0.1', '{}',
        UNIX_TIMESTAMP('2025-02-14 12:00:00') * 1000, 'DOWNLOAD',
        'zhouba@example.com', 10006, '周八', 'MOBILE');

SET FOREIGN_KEY_CHECKS = 1;
