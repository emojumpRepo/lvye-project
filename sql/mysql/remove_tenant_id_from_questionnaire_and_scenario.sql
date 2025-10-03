-- 移除问卷表和测评场景表的租户字段，使数据在所有租户间共享
-- 执行时间：2025-09-25

-- 移除问卷表的租户字段
ALTER TABLE `lvye_questionnaire` DROP COLUMN `tenant_id`;

-- 移除测评场景表的租户字段  
ALTER TABLE `lvye_assessment_scenario` DROP COLUMN `tenant_id`;
