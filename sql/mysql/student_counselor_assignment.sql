-- ====================================================
-- 学生咨询师分配关系表
-- ====================================================

-- 创建学生咨询师分配关系表
CREATE TABLE `lvye_student_counselor_assignment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `student_profile_id` BIGINT NOT NULL COMMENT '学生档案编号',
  `counselor_user_id` BIGINT NOT NULL COMMENT '负责咨询师管理员编号',
  `assignment_type` TINYINT DEFAULT 1 COMMENT '分配类型: 1-主责咨询师, 2-临时咨询师',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1-有效, 2-已失效',
  `start_date` DATE COMMENT '分配开始日期',
  `end_date` DATE COMMENT '分配结束日期',
  `assignment_reason` VARCHAR(500) COMMENT '分配原因',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  INDEX `idx_student_profile_id` (`student_profile_id`),
  INDEX `idx_counselor_user_id` (`counselor_user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_assignment_type` (`assignment_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生咨询师分配关系表';

-- 插入字典数据（供参考）
-- assignment_type: 1-主责咨询师, 2-临时咨询师
-- status: 1-有效, 2-已失效