-- 为问卷结果配置表添加等级字段
-- 执行时间：2024年

-- MySQL
ALTER TABLE `lvye_questionnaire_result_config` 
ADD COLUMN `level` varchar(10) COMMENT '等级：优秀、良好、一般、较差、很差' AFTER `is_abnormal`;

ALTER TABLE `lvye_questionnaire_result_config`
  ADD COLUMN `description` VARCHAR(2000) NULL COMMENT '描述' AFTER `is_abnormal`;

-- 是否可多命中字段（0：否，1：是）
SET @sql2 := IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'lvye_questionnaire_result_config' AND COLUMN_NAME = 'is_multi_hit') = 0,
  'ALTER TABLE lvye_questionnaire_result_config ADD COLUMN is_multi_hit TINYINT(1) NULL DEFAULT 0 COMMENT "是否可多命中（0：否，1：是）" AFTER status',
  'SELECT "is_multi_hit already exists" as message'
);
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;

-- 新增规则匹配排序字段（升序）
SET @sql := IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'lvye_questionnaire_result_config' AND COLUMN_NAME = 'match_order') = 0,
  'ALTER TABLE lvye_questionnaire_result_config ADD COLUMN match_order INT NULL DEFAULT 0 COMMENT "规则匹配排序（升序）" AFTER calculate_formula',
  'SELECT "match_order already exists" as message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 为现有数据添加默认等级（可选）
-- UPDATE `lvye_questionnaire_result_config` 
-- SET `level` = '一般' 
-- WHERE `level` IS NULL;
