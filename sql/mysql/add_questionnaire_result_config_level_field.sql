-- 为问卷结果配置表添加等级字段
-- 执行时间：2024年

-- MySQL
ALTER TABLE `lvye_questionnaire_result_config` 
ADD COLUMN `level` varchar(10) COMMENT '等级：优秀、良好、一般、较差、很差' AFTER `is_abnormal`;

ALTER TABLE `lvye_questionnaire_result_config`
  ADD COLUMN `description` VARCHAR(2000) NULL COMMENT '描述' AFTER `is_abnormal`;

-- 为现有数据添加默认等级（可选）
-- UPDATE `lvye_questionnaire_result_config` 
-- SET `level` = '一般' 
-- WHERE `level` IS NULL;
