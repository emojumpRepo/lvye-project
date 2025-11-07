-- 心理模块测试数据清理脚本
-- Psychology module test data cleanup script for H2 database
-- 按照外键依赖关系顺序删除，确保数据完整性
-- 只清理在create_tables.sql中定义的表

-- 删除测评结果表
DELETE FROM "lvye_assessment_result";

-- 删除测评任务-问卷关联表
DELETE FROM "lvye_assessment_task_questionnaire";

-- 删除测评任务表
DELETE FROM "lvye_assessment_task";

-- 删除测评场景槽位表
DELETE FROM "lvye_assessment_scenario_slot";

-- 删除测评场景表
DELETE FROM "lvye_assessment_scenario";

-- 删除问卷表
DELETE FROM "lvye_questionnaire";

-- 删除学生档案表
DELETE FROM "lvye_student_profile";

-- 重置序列（H2数据库特定）
-- 注意：H2数据库的自增序列会在删除数据后自动重置