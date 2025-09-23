-- 为测评场景表添加描述字段
-- 执行时间：2024年

-- MySQL
ALTER TABLE `lvye_assessment_scenario` 
ADD COLUMN `description` varchar(500) COMMENT '场景描述' AFTER `name`;

-- 为现有数据添加默认描述（可选）
-- UPDATE `lvye_assessment_scenario` 
-- SET `description` = CONCAT('这是', `name`, '测评场景的描述') 
-- WHERE `description` IS NULL OR `description` = '';
