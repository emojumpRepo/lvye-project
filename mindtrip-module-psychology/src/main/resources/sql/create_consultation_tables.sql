-- ====================================================
-- 心理咨询模块相关表创建脚本
-- ====================================================

-- 1. 咨询预约表
CREATE TABLE `lvye_consultation_appointment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `student_profile_id` BIGINT NOT NULL COMMENT '学生档案编号',
  `counselor_user_id` BIGINT NOT NULL COMMENT '主责咨询师（心理老师）管理员编号',
  `appointment_start_time` DATETIME NOT NULL COMMENT '预约咨询的开始时间',
  `appointment_end_time` DATETIME NOT NULL COMMENT '预约咨询的结束时间',
  `duration_minutes` INT DEFAULT 60 COMMENT '咨询时长（分钟）',
  `consultation_type` VARCHAR(255) NOT NULL COMMENT '咨询类型',
  `location` VARCHAR(255) COMMENT '咨询地点',
  `notes` TEXT COMMENT '预约时的备注信息',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(字典：appointment_status) 1-已预约/2-已完成/3-已闭环/4-已取消',
  `cancellation_reason` TEXT COMMENT '取消原因',
  `actual_time` DATETIME COMMENT '实际咨询时间（用于补录）',
  `overdue` BOOLEAN DEFAULT FALSE COMMENT '是否逾期',
  `notify_student` BOOLEAN DEFAULT TRUE COMMENT '是否通知学生',
  `remind_self` BOOLEAN DEFAULT TRUE COMMENT '是否提醒自己',
  `remind_time` INT DEFAULT 30 COMMENT '提前提醒时间（分钟）',
  `current_step` INT COMMENT '当前进度',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  INDEX `idx_student_profile_id` (`student_profile_id`),
  INDEX `idx_counselor_user_id` (`counselor_user_id`),
  INDEX `idx_appointment_time` (`appointment_time`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理咨询预约记录表';

-- 2. 咨询评估表
CREATE TABLE `lvye_consultation_assessment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `appointment_id` BIGINT NOT NULL UNIQUE COMMENT '咨询预约ID',
  `student_profile_id` BIGINT NOT NULL COMMENT '学生档案编号（冗余字段，便于查询）',
  `counselor_user_id` BIGINT NOT NULL COMMENT '评估人（心理老师）管理员编号',
  `risk_level` TINYINT NOT NULL COMMENT '风险等级(字典：risk_level)',
  `problem_types` JSON COMMENT '问题类型识别(JSON数组)',
  `follow_up_suggestion` TINYINT NOT NULL COMMENT '后续处理建议(字典：follow_up_suggestion)',
  `content` MEDIUMTEXT COMMENT '评估内容',
  `has_medical_visit` BOOLEAN COMMENT '是否就医',
  `medical_visit_record` MEDIUMTEXT COMMENT '就医记录',
  `observation_record` MEDIUMTEXT COMMENT '观察记录',
  `attachment_ids` JSON COMMENT '附件ID列表(JSON数组)',
  `draft` BOOLEAN DEFAULT TRUE COMMENT '是否为草稿',
  `submitted_at` DATETIME COMMENT '评估报告最终提交时间',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  INDEX `idx_student_profile_id` (`student_profile_id`),
  INDEX `idx_counselor_user_id` (`counselor_user_id`),
  INDEX `idx_submitted_at` (`submitted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理咨询评估报告表';

-- ====================================================
-- 危机干预模块相关表创建脚本
-- ====================================================

-- 3. 危机干预事件表（新建）
CREATE TABLE `lvye_crisis_intervention` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `event_id` VARCHAR(20) COMMENT '事件编号',
  `student_profile_id` BIGINT NOT NULL COMMENT '学生档案编号',
  `title` VARCHAR(255) COMMENT '事件标题',
  `description` TEXT COMMENT '事件描述',
  `risk_level` TINYINT COMMENT '风险等级（字典：risk_level）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（字典：intervention_status）1-已上报/2-已分配/3-处理中/4-已结案/5-持续关注',
  `handler_user_id` BIGINT COMMENT '处理人管理员编号',
  `source_type` TINYINT COMMENT '来源类型（枚举：CrisisSourceTypeEnum）',
  `reporter_user_id` BIGINT COMMENT '上报人管理员编号（任课老师/系统等）',
  `reported_at` DATETIME COMMENT '上报时间',
  `priority` TINYINT DEFAULT 2 COMMENT '优先级(字典：priority_level) 1-高/2-中/3-低',
  `location` VARCHAR(255) COMMENT '事发地点',
  `process_method` TINYINT COMMENT '处理方式(字典：process_method)',
  `process_reason` TEXT COMMENT '处理原因说明',
  `closure_summary` TEXT COMMENT '结案总结',
  `progress` INT DEFAULT 0 COMMENT '处理进度百分比',
  `process_status` TINYINT COMMENT '处理状态',
  `auto_assigned` BOOLEAN DEFAULT FALSE COMMENT '是否自动分配',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  INDEX `idx_event_id` (`event_id`),
  INDEX `idx_student_profile_id` (`student_profile_id`),
  INDEX `idx_handler_user_id` (`handler_user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='危机干预事件记录表';

-- 4. 危机事件处理过程表
CREATE TABLE `lvye_crisis_event_process` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `event_id` BIGINT NOT NULL COMMENT '危机事件ID',
  `operator_user_id` BIGINT NOT NULL COMMENT '操作人管理员编号',
  `action` VARCHAR(100) NOT NULL COMMENT '操作类型(字典：process_action)',
  `content` TEXT NOT NULL COMMENT '处理内容记录',
  `attachments` JSON COMMENT '附件URL列表(JSON数组)',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  INDEX `idx_event_id` (`event_id`),
  INDEX `idx_operator_user_id` (`operator_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='危机事件处理过程流水表';

-- 5. 危机事件评估表
CREATE TABLE `lvye_crisis_event_assessment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `event_id` BIGINT NOT NULL COMMENT '危机事件ID',
  `assessor_user_id` BIGINT NOT NULL COMMENT '评估人管理员编号',
  `assessment_type` TINYINT NOT NULL COMMENT '评估类型(字典：assessment_type) 1-阶段性/2-最终',
  `risk_level` TINYINT NOT NULL COMMENT '风险等级(字典：risk_level)',
  `problem_types` JSON COMMENT '问题类型识别(JSON数组)',
  `follow_up_suggestion` TINYINT NOT NULL COMMENT '后续建议(字典：follow_up_suggestion)',
  `content` TEXT COMMENT '评估详细内容',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  INDEX `idx_event_id` (`event_id`),
  INDEX `idx_assessor_user_id` (`assessor_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='危机事件评估记录表';

-- ====================================================
-- 状态流转记录表
-- ====================================================

-- 6. 学生状态变更历史表
CREATE TABLE `lvye_student_status_history` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `student_profile_id` BIGINT NOT NULL COMMENT '学生档案编号',
  `old_status` TINYINT COMMENT '原状态',
  `new_status` TINYINT NOT NULL COMMENT '新状态',
  `change_reason` TEXT COMMENT '变更原因',
  `effective_date` DATE COMMENT '生效日期',
  `operator_user_id` BIGINT NOT NULL COMMENT '操作人管理员编号',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  INDEX `idx_student_profile_id` (`student_profile_id`),
  INDEX `idx_operator_user_id` (`operator_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生状态变更历史表';

-- 7. 干预等级变更历史表
CREATE TABLE `lvye_intervention_level_history` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `student_profile_id` BIGINT NOT NULL COMMENT '学生档案编号',
  `old_level` TINYINT COMMENT '原等级',
  `new_level` TINYINT NOT NULL COMMENT '新等级',
  `change_reason` TEXT NOT NULL COMMENT '调整原因',
  `operator_user_id` BIGINT NOT NULL COMMENT '操作人管理员编号',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  INDEX `idx_student_profile_id` (`student_profile_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='干预等级变更历史表';

-- ====================================================
-- 字典数据插入（供参考，实际使用系统字典管理）
-- ====================================================

-- 咨询类型字典
-- consultation_type: 1-初次咨询, 2-复诊咨询, 3-紧急咨询, 4-家长咨询

-- 预约状态字典
-- appointment_status: 1-已预约, 2-已完成, 3-已闭环, 4-已取消

-- 危机干预状态字典
-- intervention_status: 1-已上报, 2-已分配, 3-处理中, 4-已结案, 5-持续关注

-- 风险等级字典
-- risk_level: 1-重大, 2-严重, 3-一般, 4-观察, 5-正常

-- 后续建议字典
-- follow_up_suggestion: 1-继续咨询, 2-继续量表测评, 3-持续观察, 4-问题基本解决, 5-转介专业治疗

-- 优先级字典
-- priority_level: 1-高, 2-中, 3-低

-- 处理方式字典
-- process_method: 1-心理访谈, 2-量表评估, 3-持续关注, 4-直接解决

-- 评估类型字典
-- assessment_type: 1-阶段性评估, 2-最终评估

-- 处理动作字典
-- process_action: 分配负责人, 更改负责人, 选择处理方式, 阶段性评估, 结案, 添加处理记录