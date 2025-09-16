-- ----------------------------
-- 为问卷表添加是否支持独立使用字段
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 为 lvye_questionnaire 表添加 support_independent_use 字段
ALTER TABLE `lvye_questionnaire` 
ADD COLUMN `support_independent_use` TINYINT DEFAULT 1 COMMENT '是否支持独立使用：0-否，1-是（默认为是）' 
AFTER `is_open`;

-- 为新字段添加索引以提高查询性能
ALTER TABLE `lvye_questionnaire` 
ADD INDEX `idx_support_independent_use` (`support_independent_use`) USING BTREE;

SET FOREIGN_KEY_CHECKS = 1;
