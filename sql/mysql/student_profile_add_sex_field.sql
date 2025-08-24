-- ----------------------------
-- 学生档案表添加性别字段
-- 使用 system_user_sex 字典
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 为 lvye_student_profile 表添加 sex 字段
ALTER TABLE `lvye_student_profile` 
ADD COLUMN `sex` TINYINT COMMENT '性别（字典：system_user_sex）' AFTER `home_address`;

-- 添加索引优化查询
CREATE INDEX `idx_sex` ON `lvye_student_profile`(`sex`);

-- 从 system_users 表同步现有数据的性别信息
UPDATE `lvye_student_profile` sp 
INNER JOIN `system_users` su ON sp.user_id = su.id 
SET sp.sex = su.sex 
WHERE sp.sex IS NULL AND su.sex IS NOT NULL;

-- 验证数据同步结果
SELECT 
    COUNT(*) as total_students,
    COUNT(sp.sex) as students_with_sex,
    COUNT(*) - COUNT(sp.sex) as students_without_sex
FROM `lvye_student_profile` sp;

-- 显示性别分布统计
SELECT 
    CASE 
        WHEN sp.sex = 1 THEN '男'
        WHEN sp.sex = 2 THEN '女'
        ELSE '未知'
    END as gender,
    COUNT(*) as count
FROM `lvye_student_profile` sp
GROUP BY sp.sex;

SET FOREIGN_KEY_CHECKS = 1;

-- 使用说明：
-- 1. 性别字段使用 system_user_sex 字典：1-男，2-女
-- 2. 执行此脚本后，现有学生档案的性别信息会从 system_users 表同步过来
-- 3. 新增学生档案时，需要同时在 lvye_student_profile 和 system_users 表中保存性别信息
-- 4. 查询时可以直接从 lvye_student_profile 表获取性别，无需关联查询
