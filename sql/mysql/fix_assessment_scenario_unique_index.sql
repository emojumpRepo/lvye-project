-- ===================================================
-- 修复测评场景表的唯一索引
-- 将 code 字段的全局唯一索引改为 (tenant_id, code) 的联合唯一索引
-- 使得同一租户内场景code唯一，不同租户间可以使用相同的code
-- ===================================================

-- 1. 删除旧的唯一索引
ALTER TABLE `lvye_assessment_scenario` DROP INDEX `uk_code`;

-- 2. 添加新的联合唯一索引 (tenant_id, code)
ALTER TABLE `lvye_assessment_scenario` ADD UNIQUE KEY `uk_tenant_code` (`tenant_id`, `code`) USING BTREE;

-- 说明：
-- 执行此脚本前，请确保数据库中没有违反新唯一约束的数据
-- 如果存在同一租户下重复的code，需要先处理这些数据