-- 维度表（全局配置）
CREATE TABLE IF NOT EXISTS lvye_questionnaire_dimension (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  questionnaire_id BIGINT NOT NULL COMMENT '问卷ID',
  dimension_name VARCHAR(100) NOT NULL COMMENT '维度名称',
  dimension_code VARCHAR(50) NOT NULL COMMENT '维度编码',
  description TEXT COMMENT '描述',
  calculate_type INT DEFAULT NULL COMMENT '兼容旧类型，可为空',
  participate_module_calc TINYINT(1) DEFAULT 0 COMMENT '是否参与模块计算',
  participate_assessment_calc TINYINT(1) DEFAULT 0 COMMENT '是否参与测评计算',
  participate_ranking TINYINT(1) DEFAULT 0 COMMENT '是否参与心理问题排行',
  sort_order INT DEFAULT 0,
  status TINYINT(1) DEFAULT 1,
  creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) DEFAULT 0,
  UNIQUE KEY uk_questionnaire_dimension (questionnaire_id, dimension_code),
  KEY idx_questionnaire_id (questionnaire_id),
  KEY idx_participate_ranking (participate_ranking)
) COMMENT='问卷维度表';

-- 维度结果配置表（全局配置）
-- 检查并添加新字段
SET @sql = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'lvye_questionnaire_result_config' AND COLUMN_NAME = 'dimension_id') = 0,
  'ALTER TABLE lvye_questionnaire_result_config ADD COLUMN dimension_id BIGINT NULL COMMENT "关联维度ID"',
  'SELECT "dimension_id already exists" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修改字段类型
ALTER TABLE lvye_questionnaire_result_config
  MODIFY COLUMN student_comment JSON NULL COMMENT '学生端评语（JSON数组）';

-- 删除老字段（改为通过 dimension_id 关联）
SET @sql = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'lvye_questionnaire_result_config' AND COLUMN_NAME = 'questionnaire_id') > 0,
  'ALTER TABLE lvye_questionnaire_result_config DROP COLUMN questionnaire_id',
  'SELECT "questionnaire_id already dropped" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'lvye_questionnaire_result_config' AND COLUMN_NAME = 'dimension_name') > 0,
  'ALTER TABLE lvye_questionnaire_result_config DROP COLUMN dimension_name',
  'SELECT "dimension_name already dropped" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1. 删除原有的 questionnaire_id 字段
ALTER TABLE `lvye_assessment_scenario_slot` DROP COLUMN `questionnaire_id`;

-- 2. 添加新的 questionnaire_ids 字段（JSON格式存储问卷ID数组）
ALTER TABLE `lvye_assessment_scenario_slot` 
ADD COLUMN `questionnaire_ids` varchar(1000) COMMENT '关联问卷ID列表（JSON格式存储，如：[1,2,3]）' AFTER `metadata_json`;

-- 维度结果（业务数据）
CREATE TABLE IF NOT EXISTS lvye_dimension_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  questionnaire_result_id BIGINT NOT NULL,
  dimension_id BIGINT NOT NULL,
  dimension_code VARCHAR(50) NOT NULL,
  risk_level TINYINT(1) DEFAULT 0 COMMENT '风险等级：1-无/低风险，2-轻度风险，3-中度风险，4-重度风险',
  score DECIMAL(10,2) NULL,
  is_abnormal TINYINT(1) DEFAULT 0,
  level VARCHAR(20) NULL,
  teacher_comment TEXT NULL,
  student_comment TEXT NULL COMMENT '学生评语（从配置中随机选择的单条评语）',
  description TEXT NULL,
  creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) DEFAULT 0,
  tenant_id BIGINT DEFAULT 0 COMMENT '租户编号',
  UNIQUE KEY uk_qr_dim (questionnaire_result_id, dimension_id),
  KEY idx_dimension_id (dimension_id),
  KEY idx_tenant_id (tenant_id)
) COMMENT='维度结果表';

-- 模块结果配置（全局配置）
CREATE TABLE IF NOT EXISTS lvye_module_result_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  scenario_slot_id BIGINT NOT NULL,
  config_name VARCHAR(100) NOT NULL,
  rule_type TINYINT(1) DEFAULT 0 COMMENT '规则类型：0-等级方面规则，1-评语方面规则',
  calculate_formula JSON NOT NULL COMMENT 'JSON 规则',
  description TEXT NULL COMMENT '配置描述',
  level VARCHAR(32) NULL COMMENT '评价等级',
  suggestions TEXT NULL COMMENT '建议文本',
  comments JSON NULL COMMENT '评语（字符串数组JSON格式）',
  status TINYINT(1) DEFAULT 1,
  creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) DEFAULT 0,
  KEY idx_scenario_slot_id (scenario_slot_id)
) COMMENT='模块结果计算配置表';

-- 测评结果配置（全局配置）
CREATE TABLE IF NOT EXISTS lvye_assessment_result_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  scenario_id BIGINT NOT NULL,
  config_name VARCHAR(100) NOT NULL,
  rule_type TINYINT(1) DEFAULT 0 COMMENT '规则类型：0-等级方面规则，1-评语方面规则',
  calculate_formula JSON NOT NULL COMMENT 'JSON 规则',
  description TEXT NULL COMMENT '配置描述',
  level VARCHAR(32) NULL COMMENT '评价等级(可选，字典或自定义文本)',
  suggestions TEXT NULL COMMENT '建议文本',
  comment TEXT NULL COMMENT '评语文本',
  status TINYINT(1) DEFAULT 1,
  creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) DEFAULT 0,
  KEY idx_scenario_id (scenario_id)
) COMMENT='测评结果计算配置表';

-- 模块结果（业务数据）
CREATE TABLE IF NOT EXISTS lvye_module_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  assessment_result_id BIGINT NOT NULL,
  scenario_slot_id BIGINT NOT NULL,
  slot_key VARCHAR(50) NOT NULL,
  module_score DECIMAL(10,2) NULL,
  risk_level INT NULL,
  teacher_comment TEXT NULL,
  student_comment TEXT NULL COMMENT '学生评语（从配置中随机选择的单条评语）',
  module_description TEXT NULL,
  creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) DEFAULT 0,
  tenant_id BIGINT DEFAULT 0 COMMENT '租户编号',
  KEY idx_assessment_result_id (assessment_result_id),
  KEY idx_scenario_slot_id (scenario_slot_id),
  KEY idx_tenant_id (tenant_id)
) COMMENT='模块结果表';


