-- 修改 lvye_assessment_scenario_slot 表，将 questionnaire_id 字段改为 questionnaire_ids 支持多问卷绑定
-- 执行时间：2025-09-25

-- 1. 删除原有的 questionnaire_id 字段
ALTER TABLE `lvye_assessment_scenario_slot` DROP COLUMN `questionnaire_id`;

-- 2. 添加新的 questionnaire_ids 字段（JSON格式存储问卷ID数组）
ALTER TABLE `lvye_assessment_scenario_slot` 
ADD COLUMN `questionnaire_ids` varchar(1000) COMMENT '关联问卷ID列表（JSON格式存储，如：[1,2,3]）' AFTER `metadata_json`;

-- 3. 创建索引以提高查询性能
CREATE INDEX `idx_questionnaire_ids` ON `lvye_assessment_scenario_slot` (`questionnaire_ids`(100));
