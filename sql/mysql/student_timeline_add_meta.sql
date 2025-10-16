-- 为学生时间线表添加meta字段
-- 执行时间：2025-01-17
-- 作者：芋道源码

-- 添加meta字段，用于存储扩展的JSON数据
ALTER TABLE lvye_student_profile_timeline 
ADD COLUMN meta JSON COMMENT '扩展元数据（JSON格式）' 
AFTER operator;

-- 注意：如果MySQL版本低于5.7，可以使用TEXT类型替代JSON
-- ALTER TABLE lvye_student_profile_timeline 
-- ADD COLUMN meta TEXT COMMENT '扩展元数据（JSON格式）' 
-- AFTER operator;