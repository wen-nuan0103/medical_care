-- 悦医健康（MedCare Online）数据库建表脚本
-- MySQL 8.0+
-- 说明：本脚本只创建数据库、数据表、索引和外键，不会删除已有表。

CREATE DATABASE IF NOT EXISTS `medical_care`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `medical_care`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1. 系统权限模块
-- =========================================================

CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) NULL COMMENT '手机号',
  `email` VARCHAR(100) NULL COMMENT '邮箱',
  `avatar_url` VARCHAR(500) NULL COMMENT '头像地址',
  `gender` VARCHAR(20) NULL COMMENT '性别',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用，1正常',
  `last_login_time` DATETIME NULL COMMENT '最近登录时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  UNIQUE KEY `uk_sys_user_phone` (`phone`),
  KEY `idx_sys_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用，1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `permission_type` VARCHAR(20) NOT NULL COMMENT 'MENU、BUTTON、API',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级权限ID',
  `path` VARCHAR(255) NULL COMMENT '前端路由或接口路径',
  `component` VARCHAR(255) NULL COMMENT '前端组件路径',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用，1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_permission_code` (`permission_code`),
  KEY `idx_sys_permission_parent` (`parent_id`),
  KEY `idx_sys_permission_type` (`permission_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_role` (`user_id`, `role_id`),
  KEY `idx_sys_user_role_role` (`role_id`),
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色表';

CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_permission` (`role_id`, `permission_id`),
  KEY `idx_sys_role_permission_permission` (`permission_id`),
  CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限表';

CREATE TABLE IF NOT EXISTS `sys_audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT NULL COMMENT '操作人用户ID',
  `username` VARCHAR(50) NULL COMMENT '操作人账号快照',
  `module` VARCHAR(50) NOT NULL COMMENT '模块',
  `action` VARCHAR(50) NOT NULL COMMENT '操作',
  `target_id` BIGINT NULL COMMENT '目标业务ID',
  `target_type` VARCHAR(50) NULL COMMENT '目标类型',
  `content` TEXT NULL COMMENT '操作内容',
  `ip` VARCHAR(64) NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) NULL COMMENT '浏览器信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_audit_user` (`user_id`),
  KEY `idx_audit_module_action` (`module`, `action`),
  KEY `idx_audit_target` (`target_type`, `target_id`),
  CONSTRAINT `fk_audit_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表';

-- =========================================================
-- 2. 基础资料模块
-- =========================================================

CREATE TABLE IF NOT EXISTS `patient_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '患者资料ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `id_card_no` VARCHAR(32) NULL COMMENT '身份证号',
  `birthday` DATE NULL COMMENT '出生日期',
  `height_cm` DECIMAL(5,2) NULL COMMENT '身高cm',
  `weight_kg` DECIMAL(5,2) NULL COMMENT '体重kg',
  `allergy_history` TEXT NULL COMMENT '过敏史',
  `medical_history` TEXT NULL COMMENT '既往病史',
  `family_history` TEXT NULL COMMENT '家族病史',
  `emergency_contact` VARCHAR(50) NULL COMMENT '紧急联系人',
  `emergency_phone` VARCHAR(20) NULL COMMENT '紧急联系电话',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_user` (`user_id`),
  CONSTRAINT `fk_patient_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者资料表';

CREATE TABLE IF NOT EXISTS `doctor_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '医生资料ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `hospital` VARCHAR(100) NOT NULL COMMENT '所属医院',
  `department` VARCHAR(100) NOT NULL COMMENT '科室',
  `title` VARCHAR(50) NOT NULL COMMENT '职称',
  `license_no` VARCHAR(100) NULL COMMENT '执业证书号',
  `specialty` TEXT NULL COMMENT '擅长方向',
  `introduction` TEXT NULL COMMENT '个人简介',
  `consultation_count` INT NOT NULL DEFAULT 0 COMMENT '接诊数量',
  `score` DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '评分',
  `service_status` TINYINT NOT NULL DEFAULT 1 COMMENT '0停诊，1开放服务',
  `audit_status` VARCHAR(30) NOT NULL DEFAULT 'APPROVED' COMMENT '入驻审核状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doctor_user` (`user_id`),
  KEY `idx_doctor_department` (`department`),
  KEY `idx_doctor_service_status` (`service_status`),
  CONSTRAINT `fk_doctor_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生资料表';

CREATE TABLE IF NOT EXISTS `pharmacist_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '药剂师资料ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `certificate_no` VARCHAR(100) NOT NULL COMMENT '执业证书号',
  `hospital_or_org` VARCHAR(100) NULL COMMENT '所属机构',
  `working_years` INT NOT NULL DEFAULT 0 COMMENT '从业年限',
  `audit_status` VARCHAR(30) NOT NULL DEFAULT 'APPROVED' COMMENT '审核状态',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0停用，1正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pharmacist_user` (`user_id`),
  UNIQUE KEY `uk_pharmacist_certificate` (`certificate_no`),
  CONSTRAINT `fk_pharmacist_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药剂师资料表';

CREATE TABLE IF NOT EXISTS `sys_dict_type` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典类型ID',
  `dict_code` VARCHAR(50) NOT NULL COMMENT '字典编码',
  `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

CREATE TABLE IF NOT EXISTS `sys_dict_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典项ID',
  `dict_type_id` BIGINT NOT NULL COMMENT '字典类型ID',
  `item_label` VARCHAR(100) NOT NULL COMMENT '展示名称',
  `item_value` VARCHAR(100) NOT NULL COMMENT '字典值',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_dict_item_type` (`dict_type_id`),
  UNIQUE KEY `uk_dict_item_value` (`dict_type_id`, `item_value`),
  CONSTRAINT `fk_dict_item_type` FOREIGN KEY (`dict_type_id`) REFERENCES `sys_dict_type` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典项表';

-- =========================================================
-- 3. 私人医生卡与订单模块
-- =========================================================

CREATE TABLE IF NOT EXISTS `doctor_card_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '卡型配置ID',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `card_type` VARCHAR(30) NOT NULL COMMENT 'ONCE、MONTH、QUARTER、HALF_YEAR、YEAR',
  `plan_name` VARCHAR(100) NOT NULL COMMENT '卡名称',
  `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
  `valid_days` INT NOT NULL COMMENT '有效天数',
  `consultation_times` INT NOT NULL COMMENT '可问诊次数',
  `total_minutes` INT NOT NULL COMMENT '总聊天时长',
  `single_minutes` INT NOT NULL COMMENT '单次聊天时长上限',
  `gift_limit_minutes` INT NOT NULL DEFAULT 0 COMMENT '医生可赠送时长上限',
  `description` VARCHAR(500) NULL COMMENT '服务说明',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0下架，1上架',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_card_plan_doctor_type` (`doctor_id`, `card_type`),
  CONSTRAINT `fk_card_plan_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生服务卡配置表';

CREATE TABLE IF NOT EXISTS `doctor_card_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `plan_id` BIGINT NOT NULL COMMENT '服务卡配置ID',
  `card_type` VARCHAR(30) NOT NULL COMMENT '卡型快照',
  `plan_name` VARCHAR(100) NOT NULL COMMENT '卡名称快照',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '应付金额',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `pay_type` VARCHAR(30) NOT NULL DEFAULT 'MOCK' COMMENT '支付方式',
  `status` VARCHAR(30) NOT NULL DEFAULT 'UNPAID' COMMENT '订单状态',
  `pay_time` DATETIME NULL COMMENT '支付时间',
  `cancel_time` DATETIME NULL COMMENT '取消时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_order_no` (`order_no`),
  KEY `idx_card_order_patient` (`patient_id`),
  KEY `idx_card_order_doctor` (`doctor_id`),
  KEY `idx_card_order_plan` (`plan_id`),
  CONSTRAINT `fk_card_order_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_card_order_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_card_order_plan` FOREIGN KEY (`plan_id`) REFERENCES `doctor_card_plan` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私人医生卡购买订单表';

CREATE TABLE IF NOT EXISTS `private_doctor_card` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务卡ID',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `plan_id` BIGINT NOT NULL COMMENT '卡型配置ID',
  `order_id` BIGINT NOT NULL COMMENT '购买订单ID',
  `card_type` VARCHAR(30) NOT NULL COMMENT '卡型',
  `start_time` DATETIME NOT NULL COMMENT '生效时间',
  `expire_time` DATETIME NOT NULL COMMENT '到期时间',
  `total_times` INT NOT NULL COMMENT '总问诊次数',
  `remaining_times` INT NOT NULL COMMENT '剩余问诊次数',
  `total_minutes` INT NOT NULL COMMENT '总分钟数',
  `remaining_minutes` INT NOT NULL COMMENT '剩余分钟数',
  `gifted_minutes` INT NOT NULL DEFAULT 0 COMMENT '已赠送分钟数',
  `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '服务卡状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_private_card_order` (`order_id`),
  KEY `idx_private_card_patient_status` (`patient_id`, `status`),
  KEY `idx_private_card_doctor_patient` (`doctor_id`, `patient_id`),
  KEY `idx_private_card_plan` (`plan_id`),
  CONSTRAINT `fk_private_card_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_private_card_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_private_card_plan` FOREIGN KEY (`plan_id`) REFERENCES `doctor_card_plan` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_private_card_order` FOREIGN KEY (`order_id`) REFERENCES `doctor_card_order` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者私人医生卡表';

-- =========================================================
-- 4. 在线问诊与聊天模块
-- =========================================================

CREATE TABLE IF NOT EXISTS `consultation_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '问诊会话ID',
  `session_no` VARCHAR(64) NOT NULL COMMENT '会话编号',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `card_id` BIGINT NOT NULL COMMENT '使用的私人医生卡ID',
  `chief_complaint` TEXT NULL COMMENT '主诉',
  `disease_desc` TEXT NULL COMMENT '病情描述',
  `status` VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT '问诊状态',
  `start_time` DATETIME NULL COMMENT '开始时间',
  `end_time` DATETIME NULL COMMENT '结束时间',
  `allowed_minutes` INT NOT NULL COMMENT '本次允许聊天分钟数',
  `used_minutes` INT NOT NULL DEFAULT 0 COMMENT '已使用分钟数',
  `summary` TEXT NULL COMMENT '医生确认后的问诊摘要',
  `ai_summary_id` BIGINT NULL COMMENT 'AI摘要日志ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consultation_session_no` (`session_no`),
  KEY `idx_consultation_patient` (`patient_id`),
  KEY `idx_consultation_doctor` (`doctor_id`),
  KEY `idx_consultation_card` (`card_id`),
  KEY `idx_consultation_status` (`status`),
  CONSTRAINT `fk_consultation_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_consultation_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_consultation_card` FOREIGN KEY (`card_id`) REFERENCES `private_doctor_card` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问诊会话表';

CREATE TABLE IF NOT EXISTS `chat_attachment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `session_id` BIGINT NOT NULL COMMENT '问诊会话ID',
  `uploader_id` BIGINT NOT NULL COMMENT '上传人用户ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '原文件名',
  `file_url` VARCHAR(500) NOT NULL COMMENT '文件地址',
  `file_type` VARCHAR(50) NULL COMMENT '文件类型',
  `file_size` BIGINT NULL COMMENT '文件大小，单位字节',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_chat_attachment_session` (`session_id`),
  KEY `idx_chat_attachment_uploader` (`uploader_id`),
  CONSTRAINT `fk_chat_attachment_session` FOREIGN KEY (`session_id`) REFERENCES `consultation_session` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_chat_attachment_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天附件表';

CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` BIGINT NOT NULL COMMENT '问诊会话ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送人用户ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收人用户ID',
  `message_type` VARCHAR(30) NOT NULL COMMENT 'TEXT、IMAGE、FILE、SYSTEM、PRESCRIPTION',
  `content` TEXT NULL COMMENT '消息内容',
  `attachment_id` BIGINT NULL COMMENT '附件ID',
  `read_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未读，1已读',
  `read_time` DATETIME NULL COMMENT '已读时间',
  `send_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_chat_message_session_time` (`session_id`, `send_time`),
  KEY `idx_chat_message_sender` (`sender_id`),
  KEY `idx_chat_message_receiver` (`receiver_id`),
  KEY `idx_chat_message_attachment` (`attachment_id`),
  CONSTRAINT `fk_chat_message_session` FOREIGN KEY (`session_id`) REFERENCES `consultation_session` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_chat_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_chat_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_chat_message_attachment` FOREIGN KEY (`attachment_id`) REFERENCES `chat_attachment` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

CREATE TABLE IF NOT EXISTS `chat_time_gift` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '赠送记录ID',
  `session_id` BIGINT NOT NULL COMMENT '问诊会话ID',
  `card_id` BIGINT NOT NULL COMMENT '服务卡ID',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `minutes` INT NOT NULL COMMENT '赠送分钟数',
  `reason` VARCHAR(500) NULL COMMENT '赠送原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_time_gift_session` (`session_id`),
  KEY `idx_time_gift_card` (`card_id`),
  KEY `idx_time_gift_doctor` (`doctor_id`),
  KEY `idx_time_gift_patient` (`patient_id`),
  CONSTRAINT `fk_time_gift_session` FOREIGN KEY (`session_id`) REFERENCES `consultation_session` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_time_gift_card` FOREIGN KEY (`card_id`) REFERENCES `private_doctor_card` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_time_gift_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_time_gift_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问诊时长赠送记录表';

-- =========================================================
-- 5. 药品库与库存模块
-- =========================================================

CREATE TABLE IF NOT EXISTS `drug_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
  `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_drug_category_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品分类表';

CREATE TABLE IF NOT EXISTS `drug` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '药品ID',
  `drug_code` VARCHAR(64) NOT NULL COMMENT '药品编码',
  `drug_name` VARCHAR(100) NOT NULL COMMENT '药品名称',
  `generic_name` VARCHAR(100) NULL COMMENT '通用名',
  `category_id` BIGINT NULL COMMENT '分类ID',
  `specification` VARCHAR(100) NOT NULL COMMENT '规格',
  `dosage_form` VARCHAR(50) NULL COMMENT '剂型',
  `manufacturer` VARCHAR(100) NULL COMMENT '生产厂家',
  `approval_no` VARCHAR(100) NULL COMMENT '批准文号',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `stock_quantity` INT NOT NULL DEFAULT 0 COMMENT '当前库存',
  `warning_threshold` INT NOT NULL DEFAULT 10 COMMENT '库存预警阈值',
  `prescription_required` TINYINT NOT NULL DEFAULT 0 COMMENT '0非处方药，1处方药',
  `insurance_covered` TINYINT NOT NULL DEFAULT 0 COMMENT '0非医保，1医保',
  `usage_instruction` TEXT NULL COMMENT '用法说明',
  `contraindication` TEXT NULL COMMENT '禁忌',
  `adverse_reaction` TEXT NULL COMMENT '不良反应',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0下架，1上架',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_drug_code` (`drug_code`),
  KEY `idx_drug_name` (`drug_name`),
  KEY `idx_drug_generic_name` (`generic_name`),
  KEY `idx_drug_category` (`category_id`),
  KEY `idx_drug_stock` (`stock_quantity`),
  CONSTRAINT `fk_drug_category` FOREIGN KEY (`category_id`) REFERENCES `drug_category` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品表';

CREATE TABLE IF NOT EXISTS `drug_stock_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存流水ID',
  `drug_id` BIGINT NOT NULL COMMENT '药品ID',
  `change_type` VARCHAR(30) NOT NULL COMMENT 'IN、OUT、LOCK、UNLOCK、ADJUST',
  `change_quantity` INT NOT NULL COMMENT '变动数量',
  `before_quantity` INT NOT NULL COMMENT '变动前库存',
  `after_quantity` INT NOT NULL COMMENT '变动后库存',
  `related_order_id` BIGINT NULL COMMENT '关联订单ID',
  `operator_id` BIGINT NULL COMMENT '操作人用户ID',
  `reason` VARCHAR(500) NULL COMMENT '原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_stock_record_drug` (`drug_id`),
  KEY `idx_stock_record_operator` (`operator_id`),
  KEY `idx_stock_record_order` (`related_order_id`),
  CONSTRAINT `fk_stock_record_drug` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_stock_record_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品库存流水表';

CREATE TABLE IF NOT EXISTS `drug_interaction_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `drug_a_id` BIGINT NOT NULL COMMENT '药品A',
  `drug_b_id` BIGINT NOT NULL COMMENT '药品B',
  `risk_level` VARCHAR(30) NOT NULL COMMENT 'LOW、MEDIUM、HIGH、FORBIDDEN',
  `description` TEXT NOT NULL COMMENT '风险说明',
  `suggestion` TEXT NULL COMMENT '药剂师建议',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_drug_interaction_pair` (`drug_a_id`, `drug_b_id`),
  KEY `idx_interaction_drug_b` (`drug_b_id`),
  CONSTRAINT `fk_interaction_drug_a` FOREIGN KEY (`drug_a_id`) REFERENCES `drug` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_interaction_drug_b` FOREIGN KEY (`drug_b_id`) REFERENCES `drug` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药物相互作用规则表';

-- =========================================================
-- 6. 电子处方与药师审核模块
-- =========================================================

CREATE TABLE IF NOT EXISTS `prescription` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '处方ID',
  `prescription_no` VARCHAR(64) NOT NULL COMMENT '处方编号',
  `session_id` BIGINT NOT NULL COMMENT '问诊会话ID',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `diagnosis` VARCHAR(255) NOT NULL COMMENT '诊断',
  `status` VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT '处方状态',
  `valid_until` DATETIME NULL COMMENT '有效期',
  `doctor_note` TEXT NULL COMMENT '医生备注',
  `patient_instruction` TEXT NULL COMMENT '面向患者的用药说明',
  `ai_explain_id` BIGINT NULL COMMENT 'AI处方解释日志ID',
  `submit_time` DATETIME NULL COMMENT '提交审核时间',
  `approve_time` DATETIME NULL COMMENT '审核通过时间',
  `expire_time` DATETIME NULL COMMENT '过期时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_prescription_no` (`prescription_no`),
  KEY `idx_prescription_session` (`session_id`),
  KEY `idx_prescription_patient` (`patient_id`),
  KEY `idx_prescription_doctor` (`doctor_id`),
  KEY `idx_prescription_status` (`status`),
  CONSTRAINT `fk_prescription_session` FOREIGN KEY (`session_id`) REFERENCES `consultation_session` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_prescription_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_prescription_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子处方主表';

CREATE TABLE IF NOT EXISTS `prescription_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `prescription_id` BIGINT NOT NULL COMMENT '处方ID',
  `drug_id` BIGINT NOT NULL COMMENT '药品ID',
  `drug_name` VARCHAR(100) NOT NULL COMMENT '药品名称快照',
  `specification` VARCHAR(100) NULL COMMENT '规格快照',
  `quantity` INT NOT NULL COMMENT '数量',
  `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价快照',
  `dosage` VARCHAR(100) NOT NULL COMMENT '单次剂量',
  `frequency` VARCHAR(100) NOT NULL COMMENT '用药频次',
  `duration_days` INT NOT NULL COMMENT '疗程天数',
  `usage_method` VARCHAR(100) NOT NULL COMMENT '用法',
  `medication_time` VARCHAR(100) NULL COMMENT '用药时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_prescription_item_prescription` (`prescription_id`),
  KEY `idx_prescription_item_drug` (`drug_id`),
  CONSTRAINT `fk_prescription_item_prescription` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_prescription_item_drug` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子处方明细表';

CREATE TABLE IF NOT EXISTS `prescription_audit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审核ID',
  `prescription_id` BIGINT NOT NULL COMMENT '处方ID',
  `pharmacist_id` BIGINT NOT NULL COMMENT '药剂师ID',
  `audit_result` VARCHAR(30) NOT NULL COMMENT 'APPROVED、REJECTED、NEED_MODIFY',
  `risk_level` VARCHAR(30) NULL COMMENT '综合风险等级',
  `interaction_result` TEXT NULL COMMENT '相互作用检查结果',
  `stock_result` TEXT NULL COMMENT '库存检查结果',
  `dosage_result` TEXT NULL COMMENT '剂量检查结果',
  `advice` TEXT NULL COMMENT '药师用药建议',
  `ai_suggestion_id` BIGINT NULL COMMENT 'AI审核辅助日志ID',
  `audit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_prescription_audit_prescription` (`prescription_id`),
  KEY `idx_prescription_audit_pharmacist` (`pharmacist_id`),
  CONSTRAINT `fk_prescription_audit_prescription` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_prescription_audit_pharmacist` FOREIGN KEY (`pharmacist_id`) REFERENCES `pharmacist_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处方审核表';

-- =========================================================
-- 7. 医保卡与购药订单模块
-- =========================================================

CREATE TABLE IF NOT EXISTS `insurance_card` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '医保卡ID',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `card_no` VARCHAR(64) NOT NULL COMMENT '模拟医保卡号',
  `holder_name` VARCHAR(50) NOT NULL COMMENT '持卡人姓名',
  `holder_id_card` VARCHAR(32) NULL COMMENT '持卡人身份证号',
  `balance` DECIMAL(10,2) NOT NULL COMMENT '模拟余额',
  `reimbursement_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.70 COMMENT '报销比例',
  `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE、FROZEN、DISABLED',
  `bind_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_insurance_card_no` (`card_no`),
  KEY `idx_insurance_card_patient` (`patient_id`),
  CONSTRAINT `fk_insurance_card_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模拟医保卡表';

CREATE TABLE IF NOT EXISTS `insurance_drug_catalog` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '目录ID',
  `drug_id` BIGINT NOT NULL COMMENT '药品ID',
  `catalog_type` VARCHAR(30) NOT NULL COMMENT 'A、B、SELF_PAY',
  `reimbursement_rate` DECIMAL(5,2) NOT NULL COMMENT '报销比例',
  `max_reimbursement_amount` DECIMAL(10,2) NULL COMMENT '单药最高报销金额',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_insurance_catalog_drug` (`drug_id`),
  CONSTRAINT `fk_insurance_catalog_drug` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医保药品目录模拟表';

CREATE TABLE IF NOT EXISTS `medicine_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '药品订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `prescription_id` BIGINT NOT NULL COMMENT '处方ID',
  `insurance_card_id` BIGINT NOT NULL COMMENT '医保卡ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '药品总金额',
  `insurance_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '医保支付金额',
  `self_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '自费金额',
  `status` VARCHAR(30) NOT NULL DEFAULT 'UNPAID' COMMENT '订单状态',
  `pay_time` DATETIME NULL COMMENT '支付时间',
  `cancel_time` DATETIME NULL COMMENT '取消时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_medicine_order_no` (`order_no`),
  KEY `idx_medicine_order_patient` (`patient_id`),
  KEY `idx_medicine_order_prescription` (`prescription_id`),
  KEY `idx_medicine_order_card` (`insurance_card_id`),
  KEY `idx_medicine_order_status` (`status`),
  CONSTRAINT `fk_medicine_order_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_medicine_order_prescription` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_medicine_order_card` FOREIGN KEY (`insurance_card_id`) REFERENCES `insurance_card` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品订单表';

CREATE TABLE IF NOT EXISTS `medicine_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` BIGINT NOT NULL COMMENT '药品订单ID',
  `drug_id` BIGINT NOT NULL COMMENT '药品ID',
  `drug_name` VARCHAR(100) NOT NULL COMMENT '药品名称快照',
  `specification` VARCHAR(100) NULL COMMENT '规格快照',
  `quantity` INT NOT NULL COMMENT '数量',
  `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '小计',
  `insurance_covered` TINYINT NOT NULL DEFAULT 0 COMMENT '是否医保',
  `insurance_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '本药品医保金额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_medicine_order_item_order` (`order_id`),
  KEY `idx_medicine_order_item_drug` (`drug_id`),
  CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `medicine_order` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_order_item_drug` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品订单明细表';

CREATE TABLE IF NOT EXISTS `insurance_payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `payment_no` VARCHAR(64) NOT NULL COMMENT '支付流水号',
  `order_id` BIGINT NOT NULL COMMENT '药品订单ID',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `insurance_card_id` BIGINT NOT NULL COMMENT '医保卡ID',
  `before_balance` DECIMAL(10,2) NOT NULL COMMENT '支付前余额',
  `paid_amount` DECIMAL(10,2) NOT NULL COMMENT '医保支付金额',
  `after_balance` DECIMAL(10,2) NOT NULL COMMENT '支付后余额',
  `status` VARCHAR(30) NOT NULL COMMENT 'SUCCESS、FAILED',
  `fail_reason` VARCHAR(500) NULL COMMENT '失败原因',
  `pay_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '支付时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_insurance_payment_no` (`payment_no`),
  KEY `idx_payment_order` (`order_id`),
  KEY `idx_payment_patient` (`patient_id`),
  KEY `idx_payment_card` (`insurance_card_id`),
  CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `medicine_order` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_payment_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_payment_card` FOREIGN KEY (`insurance_card_id`) REFERENCES `insurance_card` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医保支付记录表';

-- =========================================================
-- 8. 病患跟踪、随访与用药提醒模块
-- =========================================================

CREATE TABLE IF NOT EXISTS `health_track_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `doctor_id` BIGINT NULL COMMENT '关联医生ID',
  `record_date` DATE NOT NULL COMMENT '记录日期',
  `symptom` TEXT NULL COMMENT '症状描述',
  `temperature` DECIMAL(4,1) NULL COMMENT '体温',
  `systolic_pressure` INT NULL COMMENT '收缩压',
  `diastolic_pressure` INT NULL COMMENT '舒张压',
  `heart_rate` INT NULL COMMENT '心率',
  `blood_glucose` DECIMAL(5,2) NULL COMMENT '血糖',
  `medication_feedback` TEXT NULL COMMENT '用药反馈',
  `abnormal_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否异常',
  `ai_analysis_id` BIGINT NULL COMMENT 'AI解读日志ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_health_track_patient_date` (`patient_id`, `record_date`),
  KEY `idx_health_track_doctor` (`doctor_id`),
  CONSTRAINT `fk_health_track_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_health_track_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_profile` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康跟踪记录表';

CREATE TABLE IF NOT EXISTS `follow_up_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '随访计划ID',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `session_id` BIGINT NULL COMMENT '关联问诊ID',
  `plan_time` DATETIME NOT NULL COMMENT '随访时间',
  `content` TEXT NOT NULL COMMENT '随访内容',
  `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING、DONE、CANCELED',
  `finish_time` DATETIME NULL COMMENT '完成时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_follow_up_patient` (`patient_id`),
  KEY `idx_follow_up_doctor` (`doctor_id`),
  KEY `idx_follow_up_session` (`session_id`),
  KEY `idx_follow_up_plan_time` (`plan_time`),
  CONSTRAINT `fk_follow_up_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_follow_up_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_follow_up_session` FOREIGN KEY (`session_id`) REFERENCES `consultation_session` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='随访计划表';

CREATE TABLE IF NOT EXISTS `medication_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用药计划ID',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `prescription_id` BIGINT NOT NULL COMMENT '处方ID',
  `prescription_item_id` BIGINT NOT NULL COMMENT '处方明细ID',
  `drug_id` BIGINT NOT NULL COMMENT '药品ID',
  `drug_name` VARCHAR(100) NOT NULL COMMENT '药品名称快照',
  `start_date` DATE NOT NULL COMMENT '开始日期',
  `end_date` DATE NOT NULL COMMENT '结束日期',
  `times_per_day` INT NOT NULL COMMENT '每日次数',
  `reminder_times` VARCHAR(255) NOT NULL COMMENT '提醒时间，如08:00,13:00,20:00',
  `dosage` VARCHAR(100) NULL COMMENT '单次剂量',
  `usage_method` VARCHAR(100) NULL COMMENT '用法',
  `ai_reminder_text` VARCHAR(500) NULL COMMENT 'AI提醒文案',
  `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE、FINISHED、STOPPED',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_medication_plan_patient` (`patient_id`),
  KEY `idx_medication_plan_prescription` (`prescription_id`),
  KEY `idx_medication_plan_item` (`prescription_item_id`),
  KEY `idx_medication_plan_drug` (`drug_id`),
  CONSTRAINT `fk_medication_plan_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_medication_plan_prescription` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_medication_plan_item` FOREIGN KEY (`prescription_item_id`) REFERENCES `prescription_item` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_medication_plan_drug` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用药计划表';

CREATE TABLE IF NOT EXISTS `medication_reminder` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提醒ID',
  `plan_id` BIGINT NOT NULL COMMENT '用药计划ID',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `drug_id` BIGINT NOT NULL COMMENT '药品ID',
  `remind_time` DATETIME NOT NULL COMMENT '提醒时间',
  `status` VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING、TAKEN、MISSED、SNOOZED',
  `confirm_time` DATETIME NULL COMMENT '患者确认时间',
  `snooze_time` DATETIME NULL COMMENT '稍后提醒时间',
  `feedback` VARCHAR(500) NULL COMMENT '患者反馈',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_medication_reminder_plan` (`plan_id`),
  KEY `idx_medication_reminder_patient` (`patient_id`),
  KEY `idx_medication_reminder_drug` (`drug_id`),
  KEY `idx_medication_reminder_time` (`remind_time`),
  CONSTRAINT `fk_medication_reminder_plan` FOREIGN KEY (`plan_id`) REFERENCES `medication_plan` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_medication_reminder_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_medication_reminder_drug` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用药提醒记录表';

-- =========================================================
-- 9. 通知消息模块
-- =========================================================

CREATE TABLE IF NOT EXISTS `system_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收人用户ID',
  `sender_id` BIGINT NULL COMMENT '发送人用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `notification_type` VARCHAR(50) NOT NULL COMMENT '通知类型',
  `business_id` BIGINT NULL COMMENT '关联业务ID',
  `read_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未读，1已读',
  `read_time` DATETIME NULL COMMENT '已读时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_notification_receiver` (`receiver_id`, `read_status`),
  KEY `idx_notification_sender` (`sender_id`),
  KEY `idx_notification_type` (`notification_type`),
  CONSTRAINT `fk_notification_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_notification_sender` FOREIGN KEY (`sender_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知表';

-- =========================================================
-- 10. Spring AI 智能辅助模块
-- =========================================================

CREATE TABLE IF NOT EXISTS `ai_knowledge_document` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识文档ID',
  `title` VARCHAR(200) NOT NULL COMMENT '文档标题',
  `doc_type` VARCHAR(50) NOT NULL COMMENT 'DRUG_MANUAL、INTERACTION_RULE、PLATFORM_GUIDE、MEDICAL_FAQ',
  `content` LONGTEXT NOT NULL COMMENT '文档内容',
  `source` VARCHAR(200) NULL COMMENT '数据来源',
  `version_no` VARCHAR(50) NULL COMMENT '文档版本',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_ai_document_type` (`doc_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI知识文档表';

CREATE TABLE IF NOT EXISTS `ai_document_chunk` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '切片ID',
  `document_id` BIGINT NOT NULL COMMENT '知识文档ID',
  `chunk_no` INT NOT NULL COMMENT '切片序号',
  `chunk_content` TEXT NOT NULL COMMENT '切片内容',
  `vector_id` VARCHAR(255) NULL COMMENT '向量库中的ID',
  `token_count` INT NULL COMMENT '估算token数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_ai_chunk_document` (`document_id`),
  KEY `idx_ai_chunk_vector` (`vector_id`),
  CONSTRAINT `fk_ai_chunk_document` FOREIGN KEY (`document_id`) REFERENCES `ai_knowledge_document` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI文档切片表';

CREATE TABLE IF NOT EXISTS `ai_prompt_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `scenario` VARCHAR(50) NOT NULL COMMENT 'AI场景',
  `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `system_prompt` TEXT NOT NULL COMMENT '系统提示词',
  `user_prompt_template` TEXT NOT NULL COMMENT '用户提示词模板',
  `output_schema` TEXT NULL COMMENT '输出JSON Schema',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_ai_prompt_scenario` (`scenario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI提示词模板表';

CREATE TABLE IF NOT EXISTS `ai_suggestion_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT NOT NULL COMMENT '调用人用户ID',
  `role_code` VARCHAR(50) NOT NULL COMMENT '调用角色',
  `scenario` VARCHAR(50) NOT NULL COMMENT 'AI场景',
  `target_id` BIGINT NULL COMMENT '关联业务ID',
  `model_name` VARCHAR(100) NULL COMMENT '模型名称',
  `prompt_summary` TEXT NULL COMMENT '脱敏后的输入摘要',
  `retrieved_refs` TEXT NULL COMMENT 'RAG检索引用',
  `ai_output` LONGTEXT NULL COMMENT 'AI输出',
  `confirm_status` VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING、CONFIRMED、REJECTED',
  `confirmed_by` BIGINT NULL COMMENT '人工确认人',
  `confirm_time` DATETIME NULL COMMENT '确认时间',
  `error_message` TEXT NULL COMMENT '调用失败原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_ai_suggestion_user` (`user_id`),
  KEY `idx_ai_suggestion_role` (`role_code`),
  KEY `idx_ai_suggestion_scenario` (`scenario`),
  KEY `idx_ai_suggestion_target` (`target_id`),
  KEY `idx_ai_suggestion_confirmed_by` (`confirmed_by`),
  CONSTRAINT `fk_ai_suggestion_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ai_suggestion_confirmed_by` FOREIGN KEY (`confirmed_by`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI调用与建议日志表';

-- =========================================================
-- 11. 模拟联网医疗数据模块
-- =========================================================

CREATE TABLE IF NOT EXISTS `mock_hospital` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '医院ID',
  `hospital_name` VARCHAR(100) NOT NULL COMMENT '医院名称',
  `hospital_level` VARCHAR(50) NULL COMMENT '医院等级',
  `province` VARCHAR(50) NULL COMMENT '省份',
  `city` VARCHAR(50) NULL COMMENT '城市',
  `address` VARCHAR(255) NULL COMMENT '地址',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_mock_hospital_city` (`province`, `city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模拟医院表';

CREATE TABLE IF NOT EXISTS `mock_disease` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '疾病ID',
  `disease_code` VARCHAR(64) NOT NULL COMMENT '疾病编码',
  `disease_name` VARCHAR(100) NOT NULL COMMENT '疾病名称',
  `department` VARCHAR(100) NULL COMMENT '推荐科室',
  `description` TEXT NULL COMMENT '描述',
  `common_symptoms` TEXT NULL COMMENT '常见症状',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人用户ID',
  `update_by` BIGINT NULL COMMENT '更新人用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删，1已删',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mock_disease_code` (`disease_code`),
  KEY `idx_mock_disease_name` (`disease_name`),
  KEY `idx_mock_disease_department` (`department`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模拟疾病库表';

