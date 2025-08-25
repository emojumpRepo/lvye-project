-- 为 lvye_assessment_task 表添加缺失字段
-- 修复测评任务更新功能所需的数据库字段

-- 检查并添加任务名称字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'lvye_assessment_task'
  AND column_name = 'task_name';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE lvye_assessment_task ADD COLUMN task_name VARCHAR(120) COMMENT ''测评任务名称''',
    'SELECT ''Column task_name already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加目标对象字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'lvye_assessment_task'
  AND column_name = 'target_audience';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE lvye_assessment_task ADD COLUMN target_audience TINYINT COMMENT ''目标对象 0-学生，1-家长''',
    'SELECT ''Column target_audience already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加状态字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'lvye_assessment_task'
  AND column_name = 'status';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE lvye_assessment_task ADD COLUMN status TINYINT DEFAULT 0 COMMENT ''状态（枚举：AssessmentTaskStatusEnum）''',
    'SELECT ''Column status already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加发布人管理员编号字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'lvye_assessment_task'
  AND column_name = 'publish_user_id';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE lvye_assessment_task ADD COLUMN publish_user_id BIGINT COMMENT ''发布人管理员编号''',
    'SELECT ''Column publish_user_id already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加开始时间字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'lvye_assessment_task'
  AND column_name = 'startline';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE lvye_assessment_task ADD COLUMN startline DATETIME COMMENT ''开始时间''',
    'SELECT ''Column startline already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加截止时间字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'lvye_assessment_task'
  AND column_name = 'deadline';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE lvye_assessment_task ADD COLUMN deadline DATETIME COMMENT ''截止时间''',
    'SELECT ''Column deadline already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加任务描述字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'lvye_assessment_task'
  AND column_name = 'description';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE lvye_assessment_task ADD COLUMN description VARCHAR(2000) COMMENT ''任务描述''',
    'SELECT ''Column description already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加任务编号唯一索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'lvye_assessment_task'
  AND index_name = 'idx_task_no_tenant';

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE lvye_assessment_task ADD UNIQUE INDEX idx_task_no_tenant (task_no, tenant_id)',
    'SELECT ''Index idx_task_no_tenant already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加状态索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'lvye_assessment_task'
  AND index_name = 'idx_status';

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE lvye_assessment_task ADD INDEX idx_status (status)',
    'SELECT ''Index idx_status already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加发布人索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'lvye_assessment_task'
  AND index_name = 'idx_publish_user_id';

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE lvye_assessment_task ADD INDEX idx_publish_user_id (publish_user_id)',
    'SELECT ''Index idx_publish_user_id already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加时间范围索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'lvye_assessment_task'
  AND index_name = 'idx_time_range';

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE lvye_assessment_task ADD INDEX idx_time_range (startline, deadline)',
    'SELECT ''Index idx_time_range already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加目标对象索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'lvye_assessment_task'
  AND index_name = 'idx_target_audience';

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE lvye_assessment_task ADD INDEX idx_target_audience (target_audience)',
    'SELECT ''Index idx_target_audience already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
