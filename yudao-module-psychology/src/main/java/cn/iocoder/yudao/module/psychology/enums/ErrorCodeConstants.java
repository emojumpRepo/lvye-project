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
    ErrorCode STUDENT_IMPORT_LIST_IS_EMPTY = new ErrorCode(1_003_001_005, "导入用户数据不能为空！");
    ErrorCode STUDENT_GRADE_OR_CLASS_IS_EMPTY = new ErrorCode(1_003_001_005, "年级/班级数据不能为空！");
    ErrorCode STUDENT_GRADE_OR_CLASS_NOT_MATCH = new ErrorCode(1_003_001_005, "年级/班级数据不匹配！");

    // ========== 学生档案-单条导入专用码（保持唯一且不与既有冲突） ==========
    ErrorCode STUDENT_IMPORT_ONE_SUCCESS = new ErrorCode(1_003_001_100, "导入成功");
    ErrorCode STUDENT_IMPORT_ONE_STUDENT_NO_DUPLICATE = new ErrorCode(1_003_001_101, "学号已存在");
    ErrorCode STUDENT_IMPORT_ONE_GRADE_OR_CLASS_EMPTY = new ErrorCode(1_003_001_102, "年级/班级数据不能为空！");
    ErrorCode STUDENT_IMPORT_ONE_GRADE_OR_CLASS_NOT_MATCH = new ErrorCode(1_003_001_103, "年级/班级数据不匹配！");
    ErrorCode STUDENT_IMPORT_ONE_UNKNOWN_ERROR = new ErrorCode(1_003_001_199, "导入失败");

    // ========== 测评任务相关 1-003-002-000 ==========
    ErrorCode ASSESSMENT_TASK_NOT_EXISTS = new ErrorCode(1_003_002_000, "测评任务不存在");
    ErrorCode ASSESSMENT_TASK_CLOSED = new ErrorCode(1_003_002_001, "测评任务已关闭");
    ErrorCode ASSESSMENT_TASK_EXPIRED = new ErrorCode(1_003_002_002, "测评任务已过期");
    ErrorCode ASSESSMENT_TASK_NOT_STARTED = new ErrorCode(1_003_002_003, "测评任务未开始");
    ErrorCode ASSESSMENT_TASK_NAME_DUPLICATE = new ErrorCode(1_003_002_004, "任务名称已存在");
    ErrorCode ASSESSMENT_TASK_PARTICIPANT_EXISTS = new ErrorCode(1_003_002_005, "该学生已参与此测评任务");
    ErrorCode ASSESSMENT_TASK_PARTICIPANT_CANNOT_START = new ErrorCode(1_003_002_006, "身份不符，无法参与测评任务");

    // ========== 测评量表相关 1-003-003-000 ==========
    ErrorCode ASSESSMENT_SCALE_NOT_EXISTS = new ErrorCode(1_003_003_000, "测评量表不存在");
    ErrorCode ASSESSMENT_SCALE_DISABLED = new ErrorCode(1_003_003_001, "测评量表已禁用");
    ErrorCode ASSESSMENT_SCALE_CODE_DUPLICATE = new ErrorCode(1_003_003_002, "量表编码已存在");
    ErrorCode HARDCODED_SCALE_CANNOT_EDIT = new ErrorCode(1_003_003_003, "第一期问卷为固定内容，不支持编辑");

    // ========== 测评结果相关 1-003-004-000 ==========
    ErrorCode ASSESSMENT_RESULT_NOT_EXISTS = new ErrorCode(1_003_004_000, "测评结果不存在");
    ErrorCode ASSESSMENT_RESULT_ALREADY_EXISTS = new ErrorCode(1_003_004_001, "测评结果已存在");
    ErrorCode ASSESSMENT_NOT_COMPLETED = new ErrorCode(1_003_004_002, "测评未完成");
    ErrorCode ASSESSMENT_ALREADY_COMPLETED = new ErrorCode(1_003_004_003, "测评已完成");

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

    // ========== 问卷相关 1-003-004-000 ==========
    ErrorCode QUESTIONNAIRE_NOT_EXISTS = new ErrorCode(1_003_004_000, "问卷不存在");
    ErrorCode QUESTIONNAIRE_RESULT_CONFIG_NOT_EXISTS = new ErrorCode(1_003_004_001, "问卷结果配置不存在");
    ErrorCode QUESTIONNAIRE_NOT_PUBLISHED = new ErrorCode(1_003_010_001, "问卷未发布");
    ErrorCode QUESTIONNAIRE_ALREADY_PUBLISHED = new ErrorCode(1_003_010_002, "问卷已发布");
    ErrorCode QUESTIONNAIRE_SYNC_FAILED = new ErrorCode(1_003_010_003, "问卷同步失败");
    ErrorCode QUESTIONNAIRE_LINK_INVALID = new ErrorCode(1_003_010_004, "问卷链接无效");
    ErrorCode QUESTIONNAIRE_TITLE_DUPLICATE = new ErrorCode(1_003_010_005, "问卷标题已存在");

    // ========== 问卷结果相关 1-003-011-000 ==========
    ErrorCode QUESTIONNAIRE_RESULT_NOT_EXISTS = new ErrorCode(1_003_011_000, "问卷结果不存在");
    ErrorCode QUESTIONNAIRE_RESULT_ALREADY_EXISTS = new ErrorCode(1_003_011_001, "问卷结果已存在");
    ErrorCode QUESTIONNAIRE_RESULT_GENERATION_FAILED = new ErrorCode(1_003_011_002, "问卷结果生成失败");
    ErrorCode QUESTIONNAIRE_NOT_SUPPORTED = new ErrorCode(1_003_011_003, "问卷不支持结果生成");
    ErrorCode QUESTIONNAIRE_ANSWERS_INCOMPLETE = new ErrorCode(1_003_011_004, "问卷答案不完整");

    // ========== 测评结果扩展相关 1-003-012-000 ==========
    ErrorCode ASSESSMENT_RESULT_GENERATION_FAILED = new ErrorCode(1_003_012_000, "测评结果生成失败");
    ErrorCode ASSESSMENT_QUESTIONNAIRE_RESULTS_INCOMPLETE = new ErrorCode(1_003_012_001, "测评关联的问卷结果不完整");
    ErrorCode ASSESSMENT_CONFIG_NOT_FOUND = new ErrorCode(1_003_012_002, "测评配置未找到");

    // ========== 结果生成器相关 1-003-013-000 ==========
    ErrorCode RESULT_GENERATOR_NOT_FOUND = new ErrorCode(1_003_013_000, "结果生成器未找到");
    ErrorCode RESULT_GENERATION_CONFIG_INVALID = new ErrorCode(1_003_013_001, "结果生成配置无效");
    ErrorCode RESULT_GENERATION_TIMEOUT = new ErrorCode(1_003_013_002, "结果生成超时");
    ErrorCode RESULT_GENERATION_CONFIG_NOT_FOUND = new ErrorCode(1_003_013_003, "结果生成配置未找到");


    // ========== 测评场景相关 1-003-015-000 ==========
    ErrorCode ASSESSMENT_SCENARIO_NOT_EXISTS = new ErrorCode(1_003_015_000, "测评场景不存在");
    ErrorCode ASSESSMENT_SCENARIO_NOT_ACTIVE = new ErrorCode(1_003_015_001, "测评场景未启用");
    ErrorCode ASSESSMENT_SCENARIO_CODE_DUPLICATE = new ErrorCode(1_003_015_002, "场景编码已存在");
    ErrorCode ASSESSMENT_SCENARIO_QUESTIONNAIRE_COUNT_EXCEEDED = new ErrorCode(1_003_015_003, "问卷数量超过场景限制");
    ErrorCode ASSESSMENT_SCENARIO_SLOT_NOT_EXISTS = new ErrorCode(1_003_015_004, "场景槽位不存在");
    ErrorCode ASSESSMENT_SCENARIO_SLOT_KEY_DUPLICATE = new ErrorCode(1_003_015_005, "槽位编码在场景内重复");

    // ========== 教师相关 1-003-015-000 ==========
    ErrorCode TEACHER_NO_DUPLICATE = new ErrorCode(1_003_015_001, "工号已存在");
    ErrorCode ROLE_NOT_EXISTS = new ErrorCode(1_003_015_002, "角色不存在");
    ErrorCode TEACHER_IMPORT_LIST_IS_EMPTY = new ErrorCode(1_003_015_003, "导入用户数据不能为空！");
    ErrorCode MOBILE_NO_DUPLICATE = new ErrorCode(1_003_015_004, "手机号已存在");
    ErrorCode DEPT_NOT_EXISTS = new ErrorCode(1_003_015_005, "班级/年级不存在");
}