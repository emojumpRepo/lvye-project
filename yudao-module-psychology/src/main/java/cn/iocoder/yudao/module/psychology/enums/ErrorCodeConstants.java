package cn.iocoder.yudao.module.psychology.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Psychology 错误码枚举类
 *
 * psychology 系统，使用 1-003-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 学生档案相关 1-003-001-000 ==========
    ErrorCode STUDENT_PROFILE_NOT_EXISTS = new ErrorCode(1_003_001_000, "学生档案不存在");
    ErrorCode STUDENT_PROFILE_ALREADY_EXISTS = new ErrorCode(1_003_001_001, "学生档案已存在");
    ErrorCode STUDENT_NO_DUPLICATE = new ErrorCode(1_003_001_002, "学号已存在");
    ErrorCode STUDENT_PROFILE_GRADUATED = new ErrorCode(1_003_001_003, "学生已毕业，无法操作");
    ErrorCode PARENT_INFO_REQUIRED = new ErrorCode(1_003_001_004, "家长信息为必填项");

    // ========== 测评任务相关 1-003-002-000 ==========
    ErrorCode ASSESSMENT_TASK_NOT_EXISTS = new ErrorCode(1_003_002_000, "测评任务不存在");
    ErrorCode ASSESSMENT_TASK_CLOSED = new ErrorCode(1_003_002_001, "测评任务已关闭");
    ErrorCode ASSESSMENT_TASK_EXPIRED = new ErrorCode(1_003_002_002, "测评任务已过期");
    ErrorCode ASSESSMENT_TASK_NOT_STARTED = new ErrorCode(1_003_002_003, "测评任务未开始");
    ErrorCode ASSESSMENT_TASK_NAME_DUPLICATE = new ErrorCode(1_003_002_004, "任务名称已存在");
    ErrorCode ASSESSMENT_TASK_PARTICIPANT_EXISTS = new ErrorCode(1_003_002_005, "该学生已参与此测评任务");

    // ========== 测评量表相关 1-003-003-000 ==========
    ErrorCode ASSESSMENT_SCALE_NOT_EXISTS = new ErrorCode(1_003_003_000, "测评量表不存在");
    ErrorCode ASSESSMENT_SCALE_DISABLED = new ErrorCode(1_003_003_001, "测评量表已禁用");
    ErrorCode ASSESSMENT_SCALE_CODE_DUPLICATE = new ErrorCode(1_003_003_002, "量表编码已存在");
    ErrorCode HARDCODED_SCALE_CANNOT_EDIT = new ErrorCode(1_003_003_003, "第一期问卷为固定内容，不支持编辑");

    // ========== 测评结果相关 1-003-004-000 ==========
    ErrorCode ASSESSMENT_RESULT_NOT_EXISTS = new ErrorCode(1_003_004_000, "测评结果不存在");
    ErrorCode ASSESSMENT_RESULT_ALREADY_EXISTS = new ErrorCode(1_003_004_001, "测评结果已存在");
    ErrorCode ASSESSMENT_NOT_COMPLETED = new ErrorCode(1_003_004_002, "测评未完成");

    // ========== 心理咨询相关 1-003-005-000 ==========
    ErrorCode CONSULTATION_RECORD_NOT_EXISTS = new ErrorCode(1_003_005_000, "咨询记录不存在");
    ErrorCode CONSULTATION_APPOINTMENT_CONFLICT = new ErrorCode(1_003_005_001, "咨询预约时间冲突");

    // ========== 危机干预相关 1-003-006-000 ==========
    ErrorCode CRISIS_INTERVENTION_NOT_EXISTS = new ErrorCode(1_003_006_000, "危机干预事件不存在");
    ErrorCode CRISIS_INTERVENTION_ALREADY_HANDLED = new ErrorCode(1_003_006_001, "危机干预事件已处理");

    // ========== 通知相关 1-003-007-000 ==========
    ErrorCode NOTIFICATION_NOT_EXISTS = new ErrorCode(1_003_007_000, "通知不存在");
    ErrorCode NOTIFICATION_SEND_FAILED = new ErrorCode(1_003_007_001, "通知发送失败");

    // ========== 权限相关 1-003-008-000 ==========
    ErrorCode INSUFFICIENT_PERMISSION = new ErrorCode(1_003_008_000, "权限不足");
    ErrorCode DATA_ACCESS_DENIED = new ErrorCode(1_003_008_001, "无权访问该数据");
    ErrorCode FORCE_PASSWORD_CHANGE_REQUIRED = new ErrorCode(1_003_008_002, "首次登录需要修改密码");

    // ========== 组织架构相关 1-003-009-000 ==========
    ErrorCode DEPT_TYPE_INVALID = new ErrorCode(1_003_009_000, "部门类型无效");
    ErrorCode GRADE_GRADUATION_FAILED = new ErrorCode(1_003_009_001, "年级毕业操作失败");
    ErrorCode CLASS_TEACHER_ASSIGNMENT_FAILED = new ErrorCode(1_003_009_002, "班主任分配失败");

}