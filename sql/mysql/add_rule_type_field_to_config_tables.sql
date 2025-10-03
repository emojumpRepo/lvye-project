-- 为配置表添加 rule_type 字段
-- 执行时间：2025-09-28
-- 说明：为 lvye_assessment_result_config 和 lvye_module_result_config 表添加 rule_type 字段

-- 检查并为 lvye_assessment_result_config 表添加 rule_type 字段
SET @sql = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'lvye_assessment_result_config' AND COLUMN_NAME = 'rule_type') = 0,
  'ALTER TABLE lvye_assessment_result_config ADD COLUMN rule_type TINYINT(1) DEFAULT 0 COMMENT "规则类型：0-等级方面规则，1-评语方面规则" AFTER config_name',
  'SELECT "rule_type field already exists in lvye_assessment_result_config" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并为 lvye_module_result_config 表添加 rule_type 字段
SET @sql = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'lvye_module_result_config' AND COLUMN_NAME = 'rule_type') = 0,
  'ALTER TABLE lvye_module_result_config ADD COLUMN rule_type TINYINT(1) DEFAULT 0 COMMENT "规则类型：0-等级方面规则，1-评语方面规则" AFTER config_name',
  'SELECT "rule_type field already exists in lvye_module_result_config" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证字段是否添加成功
SELECT 
    'lvye_assessment_result_config' as table_name,
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'lvye_assessment_result_config'
    AND COLUMN_NAME = 'rule_type'

UNION ALL

SELECT 
    'lvye_module_result_config' as table_name,
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'lvye_module_result_config'
    AND COLUMN_NAME = 'rule_type'
ORDER BY table_name;

-- 为现有记录设置默认值（如果需要）
-- UPDATE lvye_assessment_result_config SET rule_type = 0 WHERE rule_type IS NULL;
-- UPDATE lvye_module_result_config SET rule_type = 0 WHERE rule_type IS NULL;
