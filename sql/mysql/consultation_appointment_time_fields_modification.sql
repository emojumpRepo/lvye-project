-- ====================================================
-- MySQL - 咨询预约表字段修改脚本
-- 将 appointment_time 字段拆分为 appointment_start_time 和 appointment_end_time
-- ====================================================

USE `ruoyi-vue-pro`;

-- 1. 添加新字段
ALTER TABLE `lvye_consultation_appointment` 
ADD COLUMN `appointment_start_time` DATETIME COMMENT '预约咨询的开始时间' AFTER `counselor_user_id`,
ADD COLUMN `appointment_end_time` DATETIME COMMENT '预约咨询的结束时间' AFTER `appointment_start_time`;

-- 2. 数据迁移：将原有的 appointment_time 数据迁移到新字段
-- 先确认要更新的记录数量
-- SELECT COUNT(*) FROM `lvye_consultation_appointment` WHERE `appointment_time` IS NOT NULL AND `appointment_start_time` IS NULL;

-- 安全的数据迁移（只更新新字段为NULL的记录）
UPDATE `lvye_consultation_appointment` 
SET `appointment_start_time` = `appointment_time`,
    `appointment_end_time` = DATE_ADD(`appointment_time`, INTERVAL COALESCE(`duration_minutes`, 60) MINUTE)
WHERE `appointment_time` IS NOT NULL 
  AND `appointment_start_time` IS NULL 
  AND `appointment_end_time` IS NULL;

-- 3. 设置新字段为非空（在数据迁移完成后）
ALTER TABLE `lvye_consultation_appointment` 
MODIFY COLUMN `appointment_start_time` DATETIME NOT NULL COMMENT '预约咨询的开始时间',
MODIFY COLUMN `appointment_end_time` DATETIME NOT NULL COMMENT '预约咨询的结束时间';

-- 4. 添加新的索引
ALTER TABLE `lvye_consultation_appointment` 
ADD INDEX `idx_appointment_start_time` (`appointment_start_time`),
ADD INDEX `idx_appointment_end_time` (`appointment_end_time`);

-- 5. 删除旧字段和旧索引
ALTER TABLE `lvye_consultation_appointment` 
DROP INDEX `idx_appointment_time`,
DROP COLUMN `appointment_time`;
