-- ----------------------------
-- 学生档案表字段扩展
-- 添加出生日期、家庭住址、特殊标记字段
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 为 lvye_student_profile 表添加新字段
ALTER TABLE `lvye_student_profile`
ADD COLUMN `birth_date` DATE COMMENT '出生日期' AFTER `name`,
ADD COLUMN `home_address` VARCHAR(500) COMMENT '家庭住址' AFTER `birth_date`,
ADD COLUMN `sex` TINYINT COMMENT '性别（字典：system_user_sex）' AFTER `home_address`,
ADD COLUMN `special_marks` VARCHAR(500) DEFAULT NULL COMMENT '特殊标记（多选，逗号分隔字典键值）' AFTER `risk_level`;

-- 添加索引优化查询
CREATE INDEX `idx_birth_date` ON `lvye_student_profile`(`birth_date`);
CREATE INDEX `idx_sex` ON `lvye_student_profile`(`sex`);
CREATE INDEX `idx_special_marks` ON `lvye_student_profile`(`special_marks`);

-- 从 system_users 表同步现有数据的性别信息
UPDATE `lvye_student_profile` sp
INNER JOIN `system_users` su ON sp.user_id = su.id
SET sp.sex = su.sex
WHERE sp.sex IS NULL AND su.sex IS NOT NULL;

-- 创建完整的学生档案表（如果不存在）
CREATE TABLE IF NOT EXISTS `lvye_student_profile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT COMMENT '用户编号（学生），关联 system_users 的 id',
    `student_no` VARCHAR(60) NOT NULL COMMENT '学号',
    `name` VARCHAR(120) NOT NULL COMMENT '姓名',
    `birth_date` DATE COMMENT '出生日期',
    `home_address` VARCHAR(500) COMMENT '家庭住址',
    `sex` TINYINT COMMENT '性别（字典：system_user_sex）',
    `grade_dept_id` BIGINT NOT NULL COMMENT '年级部门编号，关联 system_dept.id',
    `class_dept_id` BIGINT NOT NULL COMMENT '班级部门编号，关联 system_dept.id',
    `graduation_status` TINYINT COMMENT '毕业状态（字典：graduation_status）',
    `psychological_status` TINYINT COMMENT '心理状态',
    `risk_level` TINYINT COMMENT '风险等级',
    `special_marks` VARCHAR(500) DEFAULT NULL COMMENT '特殊标记（多选，逗号分隔字典键值）',
    `remark` VARCHAR(500) COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT DEFAULT 0 COMMENT '租户编号',
    
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_student_no` (`student_no`, `tenant_id`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE,
    KEY `idx_grade_dept_id` (`grade_dept_id`) USING BTREE,
    KEY `idx_class_dept_id` (`class_dept_id`) USING BTREE,
    KEY `idx_birth_date` (`birth_date`) USING BTREE,
    KEY `idx_special_marks` (`special_marks`) USING BTREE,
    KEY `idx_graduation_status` (`graduation_status`) USING BTREE,
    KEY `idx_psychological_status` (`psychological_status`) USING BTREE,
    KEY `idx_risk_level` (`risk_level`) USING BTREE,
    KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生档案表';

-- 学生档案历史记录表也需要添加对应字段
ALTER TABLE `lvye_student_profile_record` 
ADD COLUMN `birth_date` DATE COMMENT '出生日期' AFTER `student_no`,
ADD COLUMN `home_address` VARCHAR(500) COMMENT '家庭住址' AFTER `birth_date`,
ADD COLUMN `special_marks` VARCHAR(500) DEFAULT NULL COMMENT '特殊标记（多选，逗号分隔字典键值）' AFTER `class_dept_id`;

-- 创建完整的学生档案历史记录表（如果不存在）
CREATE TABLE IF NOT EXISTS `lvye_student_profile_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT COMMENT '用户id',
    `student_no` VARCHAR(60) COMMENT '学号',
    `birth_date` DATE COMMENT '出生日期',
    `home_address` VARCHAR(500) COMMENT '家庭住址',
    `study_year` VARCHAR(50) COMMENT '学年',
    `grade_dept_id` BIGINT COMMENT '年级部门编号',
    `class_dept_id` BIGINT COMMENT '班级部门编号',
    `special_marks` VARCHAR(500) DEFAULT NULL COMMENT '特殊标记（多选，逗号分隔字典键值）',
    `remark` VARCHAR(500) COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT DEFAULT 0 COMMENT '租户编号',
    
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE,
    KEY `idx_student_no` (`student_no`) USING BTREE,
    KEY `idx_study_year` (`study_year`) USING BTREE,
    KEY `idx_grade_dept_id` (`grade_dept_id`) USING BTREE,
    KEY `idx_class_dept_id` (`class_dept_id`) USING BTREE,
    KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生历史档案表';

-- 插入特殊标记字典数据
INSERT IGNORE INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
('学生特殊标记', 'student_special_mark', 0, '学生特殊标记类型：家庭困难、行为异常、心理风险等（多选用逗号分隔）', 'admin', NOW(), 'admin', NOW(), b'0', 1);

INSERT IGNORE INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
(1, '身体残疾', '1', 'student_special_mark', 0, 'primary', '', '身体功能障碍、需要特殊照顾的学生', 'admin', NOW(), 'admin', NOW(), b'0', 1),
(2, '学习困难', '2', 'student_special_mark', 0, 'info', '', '学习能力较弱、成绩长期落后、需要特殊辅导的学生', 'admin', NOW(), 'admin', NOW(), b'0', 1),
(3, '心理风险', '3', 'student_special_mark', 0, 'error', '', '抑郁倾向、焦虑症状、自伤行为等存在心理健康风险的学生', 'admin', NOW(), 'admin', NOW(), b'0', 1),
(4, '家庭困难', '4', 'student_special_mark', 0, 'warning', '', '经济困难、单亲家庭、留守儿童等家庭条件特殊的学生', 'admin', NOW(), 'admin', NOW(), b'0', 1),
(5, '行为异常', '5', 'student_special_mark', 0, 'danger', '', '课堂纪律差、攻击性行为、社交障碍等行为表现异常的学生', 'admin', NOW(), 'admin', NOW(), b'0', 1),
(6, '天赋异禀', '6', 'student_special_mark', 0, 'success', '', '在某些领域表现突出、具有特殊天赋的学生', 'admin', NOW(), 'admin', NOW(), b'0', 1),
(7, '转学生', '7', 'student_special_mark', 0, 'default', '', '从其他学校转入的学生，需要适应期关注', 'admin', NOW(), 'admin', NOW(), b'0', 1),
(8, '复读生', '8', 'student_special_mark', 0, 'default', '', '重新就读同一年级的学生', 'admin', NOW(), 'admin', NOW(), b'0', 1);

SET FOREIGN_KEY_CHECKS = 1;

-- 使用说明：
-- 1. 特殊标记使用数字字典键值，支持多选组合（逗号分隔）
-- 2. 字典键值：1-身体残疾，2-学习困难，3-心理风险，4-家庭困难，5-行为异常，6-天赋异禀，7-转学生，8-复读生
-- 3. 存储示例：
--    - 单个标记：'1' (身体残疾)
--    - 多个标记：'2,3' (学习困难+心理风险)
--    - 多个标记：'1,4,5' (身体残疾+家庭困难+行为异常)
-- 4. 查询示例：
--    - 查询有身体残疾标记的学生：WHERE FIND_IN_SET('1', special_marks) > 0
--    - 查询有心理风险标记的学生：WHERE FIND_IN_SET('3', special_marks) > 0
--    - 查询同时有学习困难和心理风险的学生：WHERE FIND_IN_SET('2', special_marks) > 0 AND FIND_IN_SET('3', special_marks) > 0
-- 5. 数据示例：
--    INSERT INTO lvye_student_profile (student_no, name, special_marks) VALUES 
--    ('2024001', '张三', '2,3'),  -- 学习困难+心理风险
--    ('2024002', '李四', '5'),    -- 行为异常
--    ('2024003', '王五', '1,7');  -- 身体残疾+转学生