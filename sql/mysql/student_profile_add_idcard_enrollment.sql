-- 为学生档案表添加身份证号和届别字段
-- 执行时间：2025-01-17
-- 作者：芋道源码

-- 添加身份证号字段（18位）
ALTER TABLE lvye_student_profile 
ADD COLUMN id_card VARCHAR(18) COMMENT '身份证号' 
AFTER special_marks;

-- 添加届别（入学年份）字段
ALTER TABLE lvye_student_profile 
ADD COLUMN enrollment_year INT COMMENT '届别（入学年份）' 
AFTER id_card;

-- 为身份证号添加唯一索引，确保不重复
ALTER TABLE lvye_student_profile 
ADD UNIQUE INDEX idx_id_card (id_card);

-- 为届别添加索引，便于查询特定届别的学生
ALTER TABLE lvye_student_profile 
ADD INDEX idx_enrollment_year (enrollment_year);