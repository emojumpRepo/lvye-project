-- OAuth2 数据库字段缺失错误修复脚本
-- 修复 system_oauth2_access_token 和 system_oauth2_refresh_token 表缺失 is_parent 字段的问题
-- 
-- 错误详情: Unknown column 'is_parent' in 'field list'
-- 修复日期: 2025-08-25
-- 作者: 系统自动修复

-- 修复 system_oauth2_access_token 表缺失 is_parent 字段
-- 检查字段是否存在，避免重复添加
SET @count = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'system_oauth2_access_token' 
    AND COLUMN_NAME = 'is_parent'
);

SET @sql = IF(@count = 0, 
    'ALTER TABLE `system_oauth2_access_token` ADD COLUMN `is_parent` tinyint DEFAULT NULL COMMENT ''是否家长登录'' AFTER `expires_time`;', 
    'SELECT ''字段 is_parent 已存在于 system_oauth2_access_token 表中'' AS message;'
);

PREPARE stmt1 FROM @sql;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- 修复 system_oauth2_refresh_token 表缺失 is_parent 字段
SET @count = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'system_oauth2_refresh_token' 
    AND COLUMN_NAME = 'is_parent'
);

SET @sql = IF(@count = 0, 
    'ALTER TABLE `system_oauth2_refresh_token` ADD COLUMN `is_parent` tinyint DEFAULT NULL COMMENT ''是否家长登录'' AFTER `expires_time`;', 
    'SELECT ''字段 is_parent 已存在于 system_oauth2_refresh_token 表中'' AS message;'
);

PREPARE stmt2 FROM @sql;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- 验证修复结果
SELECT 
    'system_oauth2_access_token' as table_name,
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT, 
    COLUMN_COMMENT
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'system_oauth2_access_token'
AND COLUMN_NAME = 'is_parent'

UNION ALL

SELECT 
    'system_oauth2_refresh_token' as table_name,
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT, 
    COLUMN_COMMENT
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'system_oauth2_refresh_token'
AND COLUMN_NAME = 'is_parent';