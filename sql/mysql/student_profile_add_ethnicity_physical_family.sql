-- ----------------------------
-- 学生档案表字段扩展
-- 添加民族、身高、体重、家中孩子情况字段
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 为 lvye_student_profile 表添加新字段
ALTER TABLE `lvye_student_profile`
ADD COLUMN `ethnicity` TINYINT COMMENT '民族（字典：student_ethnicity）' AFTER `sex`,
ADD COLUMN `height` DECIMAL(5,2) COMMENT '身高（厘米）' AFTER `ethnicity`,
ADD COLUMN `weight` DECIMAL(5,2) COMMENT '体重（千克）' AFTER `height`,
ADD COLUMN `actual_age` TINYINT COMMENT '实际年龄（岁）' AFTER `weight`,
ADD COLUMN `family_children_info` JSON COMMENT '家中孩子情况（JSON格式）' AFTER `actual_age`;

-- 添加索引优化查询
CREATE INDEX `idx_ethnicity` ON `lvye_student_profile`(`ethnicity`);
CREATE INDEX `idx_height` ON `lvye_student_profile`(`height`);
CREATE INDEX `idx_weight` ON `lvye_student_profile`(`weight`);
CREATE INDEX `idx_actual_age` ON `lvye_student_profile`(`actual_age`);

-- 学生档案历史记录表也需要添加对应字段
ALTER TABLE `lvye_student_profile_record` 
ADD COLUMN `ethnicity` TINYINT COMMENT '民族（字典：student_ethnicity）' AFTER `home_address`,
ADD COLUMN `height` DECIMAL(5,2) COMMENT '身高（厘米）' AFTER `ethnicity`,
ADD COLUMN `weight` DECIMAL(5,2) COMMENT '体重（千克）' AFTER `height`,
ADD COLUMN `actual_age` TINYINT COMMENT '实际年龄（岁）' AFTER `weight`,
ADD COLUMN `family_children_info` JSON COMMENT '家中孩子情况（JSON格式）' AFTER `actual_age`;

-- 插入民族字典类型数据
INSERT IGNORE INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
('学生民族', 'student_ethnicity', 0, '学生民族类型：汉族、少数民族', '1', NOW(), '1', NOW(), b'0');

-- 插入民族字典数据
INSERT IGNORE INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(1, '汉族', '1', 'student_ethnicity', 0, 'primary', '', '汉族学生', '1', NOW(), 'admin', NOW(), b'0'),
(2, '少数民族', '2', 'student_ethnicity', 0, 'info', '', '少数民族学生', '1', NOW(), 'admin', NOW(), b'0');

SET FOREIGN_KEY_CHECKS = 1;

-- 使用说明：
-- 1. 民族字段使用数字枚举：
--    - 1: 汉族
--    - 2: 少数民族
-- 
-- 2. 身高字段：
--    - 数据类型：DECIMAL(5,2)，支持小数点后2位
--    - 单位：厘米
--    - 示例：175.50 表示175.5厘米
-- 
-- 3. 体重字段：
--    - 数据类型：DECIMAL(5,2)，支持小数点后2位
--    - 单位：千克
--    - 示例：65.80 表示65.8千克
--
-- 4. 实际年龄字段：
--    - 数据类型：TINYINT，整数类型
--    - 单位：岁
--    - 示例：16 表示16岁
--
-- 5. 家中孩子情况字段（JSON格式）：
--    - isOnlyChild: 是否是家里唯一的孩子（1:是，0:否）
--    - childrenCount: 家里一共有几个孩子（整数）
--    - birthOrder: 您是家里第几个出生的孩子（整数）
--    - ageGapToSecond: 与家里第二个孩子相差几岁（整数，如果是独生子女则为0）
-- 
-- 6. JSON数据示例：
--    -- 独生子女
--    '{"isOnlyChild": 1, "childrenCount": 1, "birthOrder": 1, "ageGapToSecond": 0}'
--    
--    -- 有两个孩子，这是第一个
--    '{"isOnlyChild": 0, "childrenCount": 2, "birthOrder": 1, "ageGapToSecond": 3}'
--    
--    -- 有三个孩子，这是第二个
--    '{"isOnlyChild": 0, "childrenCount": 3, "birthOrder": 2, "ageGapToSecond": 0}'
--    
--    -- 有三个孩子，这是第三个
--    '{"isOnlyChild": 0, "childrenCount": 3, "birthOrder": 3, "ageGapToSecond": 2}'
-- 
-- 7. 数据插入示例：
--    INSERT INTO lvye_student_profile (student_no, name, ethnicity, height, weight, actual_age, family_children_info) VALUES 
--    ('2024001', '张三', 1, 175.50, 65.80, 16, '{"isOnlyChild": 1, "childrenCount": 1, "birthOrder": 1, "ageGapToSecond": 0}'),
--    ('2024002', '李四', 2, 168.00, 58.50, 17, '{"isOnlyChild": 0, "childrenCount": 2, "birthOrder": 1, "ageGapToSecond": 3}'),
--    ('2024003', '王五', 1, 182.30, 72.10, 15, '{"isOnlyChild": 0, "childrenCount": 3, "birthOrder": 2, "ageGapToSecond": 0}');
-- 
-- 8. JSON查询示例：
--    -- 查询独生子女
--    SELECT * FROM lvye_student_profile WHERE JSON_EXTRACT(family_children_info, '$.isOnlyChild') = true;
--    
--    -- 查询家里有3个孩子的学生
--    SELECT * FROM lvye_student_profile WHERE JSON_EXTRACT(family_children_info, '$.childrenCount') = 3;
--    
--    -- 查询是家里第一个出生的学生
--    SELECT * FROM lvye_student_profile WHERE JSON_EXTRACT(family_children_info, '$.birthOrder') = 1;
--    
--    -- 查询与第二个孩子相差2岁的学生
--    SELECT * FROM lvye_student_profile WHERE JSON_EXTRACT(family_children_info, '$.ageGapToSecond') = 2;
--    
--    -- 查询身高在170-180厘米之间的学生
--    SELECT * FROM lvye_student_profile WHERE height BETWEEN 170.00 AND 180.00;
--    
--    -- 查询体重在60-70千克之间的学生
--    SELECT * FROM lvye_student_profile WHERE weight BETWEEN 60.00 AND 70.00;
--    
--    -- 查询年龄在15-18岁之间的学生
--    SELECT * FROM lvye_student_profile WHERE actual_age BETWEEN 15 AND 18;
--    
--    -- 查询少数民族学生
--    SELECT * FROM lvye_student_profile WHERE ethnicity = 2;
