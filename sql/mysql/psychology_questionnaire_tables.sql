-- ----------------------------
-- 问卷管理相关表定义（统一使用 lvye_ 前缀与项目规范对齐）
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for lvye_questionnaire
-- ----------------------------
DROP TABLE IF EXISTS `lvye_questionnaire`;
CREATE TABLE `lvye_questionnaire` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '问卷ID',
    `title` VARCHAR(255) NOT NULL COMMENT '问卷标题',
    `description` TEXT COMMENT '问卷描述',
    `questionnaire_type` TINYINT NOT NULL COMMENT '问卷类型：1-心理健康，2-学习适应，3-人际关系，4-情绪管理',
    `target_audience` TINYINT NOT NULL COMMENT '目标对象：1-学生，2-家长',
    `external_id` VARCHAR(100) COMMENT '外部系统问卷ID',
    `external_link` VARCHAR(500) COMMENT '外部问卷链接',
    `survey_code` VARCHAR(128) COMMENT '问卷编码',
    `question_count` INT COMMENT '题目数量',
    `estimated_duration` INT COMMENT '预计用时（分钟）',
    `content` LONGTEXT COMMENT '问卷内容（题目、选项等）',
    `scoring_rules` TEXT COMMENT '评分规则配置',
    `result_template` TEXT COMMENT '结果报告模板',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-已暂停，3-已关闭',
    `is_open` TINYINT DEFAULT 1 COMMENT '是否开放：0-否，1-是',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT DEFAULT 0 COMMENT '租户编号',
    
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_questionnaire_type` (`questionnaire_type`) USING BTREE,
    KEY `idx_target_audience` (`target_audience`) USING BTREE,
    KEY `idx_status` (`status`) USING BTREE,
    KEY `idx_is_open` (`is_open`) USING BTREE,
    KEY `idx_external_id` (`external_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='心理问卷表';

-- ----------------------------
-- Table structure for lvye_assessment_scenario
-- ----------------------------
DROP TABLE IF EXISTS `lvye_assessment_scenario`;
CREATE TABLE `lvye_assessment_scenario` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '场景ID',
    `code` VARCHAR(64) NOT NULL COMMENT '场景编码，唯一',
    `name` VARCHAR(128) NOT NULL COMMENT '场景名称',
    `max_questionnaire_count` INT NULL COMMENT '最大问卷数量限制，NULL 表示不限制',
    `frontend_route` VARCHAR(128) NOT NULL COMMENT '前端路由标识',
    `is_active` BIT(1) NOT NULL DEFAULT b'1' COMMENT '是否启用：1-启用，0-停用',
    `metadata_json` JSON NULL COMMENT '扩展配置',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `code`) USING BTREE,
    KEY `idx_is_active` (`is_active`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测评场景定义表';

-- ----------------------------
-- Table structure for lvye_assessment_scenario_slot
-- ----------------------------
DROP TABLE IF EXISTS `lvye_assessment_scenario_slot`;
CREATE TABLE `lvye_assessment_scenario_slot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '槽位ID',
    `scenario_id` BIGINT NOT NULL COMMENT '场景ID',
    `slot_key` VARCHAR(64) NOT NULL COMMENT '槽位编码，在场景内唯一',
    `slot_name` VARCHAR(128) NOT NULL COMMENT '槽位名称',
    `slot_order` INT NOT NULL DEFAULT 0 COMMENT '槽位顺序',
    `questionnaire_id` BIGINT NULL COMMENT '问卷ID',
    `metadata_json` JSON NULL COMMENT '扩展配置',
    `allowed_questionnaire_types` VARCHAR(256) NULL COMMENT '允许的问卷类型，逗号分隔',
    `frontend_component` VARCHAR(128) NULL COMMENT '前端组件标识',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_scenario_slot` (`scenario_id`, `slot_key`) USING BTREE,
    KEY `idx_scenario_id` (`scenario_id`) USING BTREE,
    KEY `idx_questionnaire_id` (`questionnaire_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测评场景槽位定义表';

-- ----------------------------
-- Table structure for lvye_assessment_task_questionnaire
-- ----------------------------
DROP TABLE IF EXISTS `lvye_assessment_task_questionnaire`;
CREATE TABLE `lvye_assessment_task_questionnaire` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_no` VARCHAR(64) NOT NULL COMMENT '任务编号',
    `questionnaire_id` BIGINT NOT NULL COMMENT '问卷ID',
    `slot_key` VARCHAR(64) NULL COMMENT '槽位标识',
    `slot_order` INT NULL COMMENT '槽位顺序',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_task_no` (`task_no`) USING BTREE,
    KEY `idx_questionnaire_id` (`questionnaire_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测评任务-问卷关联表';

-- 若关联表已存在但缺少槽位列，可执行以下增量脚本：
-- ALTER TABLE lvye_assessment_task_questionnaire ADD COLUMN `slot_key` VARCHAR(64) NULL;
-- ALTER TABLE lvye_assessment_task_questionnaire ADD COLUMN `slot_order` INT NULL;

-- ----------------------------
-- Table structure for lvye_questionnaire_result
-- ----------------------------
DROP TABLE IF EXISTS `lvye_questionnaire_result`;
CREATE TABLE `lvye_questionnaire_result` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '结果ID',
    `questionnaire_id` BIGINT NOT NULL COMMENT '问卷ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `assessment_task_id` BIGINT COMMENT '关联的测评任务ID（如果是测评任务的一部分）',
    `assessment_result_id` BIGINT COMMENT '关联的测评结果ID',
    `participant_type` TINYINT DEFAULT 1 COMMENT '参与者类型：1-学生本人，2-家长代答',
    `answers` LONGTEXT COMMENT '答题详情',
    `raw_score` DECIMAL(10,2) COMMENT '原始得分',
    `standard_score` DECIMAL(10,2) COMMENT '标准分',
    `percentile_rank` DECIMAL(5,2) COMMENT '百分位排名',
    `risk_level` TINYINT COMMENT '风险等级：1-正常，2-关注，3-预警，4-高危',
    `level_description` VARCHAR(500) COMMENT '等级描述',
    `dimension_scores` JSON COMMENT '各维度得分',
    `result_data` JSON COMMENT '详细结果数据',
    `report_content` LONGTEXT COMMENT '结果报告内容',
    `suggestions` TEXT COMMENT '建议内容',
    `completed_time` DATETIME COMMENT '完成时间',
    `generation_status` TINYINT DEFAULT 0 COMMENT '生成状态：0-待生成，1-生成中，2-已生成，3-生成失败',
    `generation_time` DATETIME COMMENT '结果生成时间',
    `generation_error` TEXT COMMENT '生成错误信息',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT DEFAULT 0 COMMENT '租户编号',
    
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_questionnaire_id` (`questionnaire_id`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE,
    KEY `idx_assessment_task_id` (`assessment_task_id`) USING BTREE,
    KEY `idx_assessment_result_id` (`assessment_result_id`) USING BTREE,
    KEY `idx_participant_type` (`participant_type`) USING BTREE,
    KEY `idx_risk_level` (`risk_level`) USING BTREE,
    KEY `idx_generation_status` (`generation_status`) USING BTREE,
    KEY `idx_completed_time` (`completed_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷结果表';

-- ----------------------------
-- Table structure for lvye_result_generation_config
-- ----------------------------
DROP TABLE IF EXISTS `lvye_result_generation_config`;
CREATE TABLE `lvye_result_generation_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_name` VARCHAR(200) NOT NULL COMMENT '配置名称',
    `config_type` TINYINT NOT NULL COMMENT '配置类型：1-单问卷结果，2-组合测评结果',
    `questionnaire_id` BIGINT COMMENT '问卷ID（单问卷配置）',
    `assessment_template_id` BIGINT COMMENT '测评模板ID（组合配置）',
    `version` VARCHAR(50) NOT NULL COMMENT '配置版本',
    `scoring_algorithm` TEXT COMMENT '评分算法配置',
    `risk_level_rules` TEXT COMMENT '风险等级判定规则',
    `weight_config` TEXT COMMENT '权重配置（组合测评用）',
    `report_template` TEXT COMMENT '报告模板配置',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否激活：1-激活，0-停用',
    `effective_time` DATETIME COMMENT '生效时间',
    `expire_time` DATETIME COMMENT '过期时间',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT DEFAULT 0 COMMENT '租户编号',
    
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_config_version` (`config_name`, `version`) USING BTREE,
    KEY `idx_config_type` (`config_type`) USING BTREE,
    KEY `idx_questionnaire_id` (`questionnaire_id`) USING BTREE,
    KEY `idx_assessment_template_id` (`assessment_template_id`) USING BTREE,
    KEY `idx_is_active` (`is_active`) USING BTREE,
    KEY `idx_effective_time` (`effective_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='结果生成配置表';

-- ----------------------------
-- Table structure for lvye_questionnaire_access
-- ----------------------------
DROP TABLE IF EXISTS `lvye_questionnaire_access`;
CREATE TABLE `lvye_questionnaire_access` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '访问记录ID',
    `questionnaire_id` BIGINT NOT NULL COMMENT '问卷ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `access_time` DATETIME NOT NULL COMMENT '访问时间',
    `access_ip` VARCHAR(50) COMMENT '访问IP',
    `user_agent` TEXT COMMENT '用户代理',
    `access_source` TINYINT COMMENT '访问来源：1-直接访问，2-测评任务，3-推荐链接',
    `session_duration` INT COMMENT '会话时长（秒）',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` BIGINT DEFAULT 0 COMMENT '租户编号',
    
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_questionnaire_id` (`questionnaire_id`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE,
    KEY `idx_access_time` (`access_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷访问记录表';

-- ----------------------------
-- 扩展现有测评结果表字段（统一为 lvye_ 前缀）
-- ----------------------------
ALTER TABLE lvye_assessment_result ADD COLUMN `questionnaire_results` JSON COMMENT '关联的问卷结果汇总';
-- 任务表新增场景ID列（若已存在可忽略）
ALTER TABLE lvye_assessment_task ADD COLUMN `scenario_id` BIGINT NULL COMMENT '场景ID';

ALTER TABLE lvye_assessment_result ADD COLUMN `combined_risk_level` TINYINT COMMENT '综合风险等级';
ALTER TABLE lvye_assessment_result ADD COLUMN `risk_factors` JSON COMMENT '风险因素分析';
ALTER TABLE lvye_assessment_result ADD COLUMN `intervention_suggestions` TEXT COMMENT '干预建议';
ALTER TABLE lvye_assessment_result ADD COLUMN `generation_config_version` VARCHAR(50) COMMENT '生成规则版本';

SET FOREIGN_KEY_CHECKS = 1;