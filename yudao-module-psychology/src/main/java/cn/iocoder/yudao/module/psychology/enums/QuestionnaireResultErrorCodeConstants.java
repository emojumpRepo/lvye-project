package cn.iocoder.yudao.module.psychology.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 问卷结果错误码枚举类
 *
 * psychology 系统，使用 2-003-xxx 段
 */
public interface QuestionnaireResultErrorCodeConstants {

    // ========== 问卷结果相关 2-003-000 ==========
    ErrorCode QUESTIONNAIRE_RESULT_NOT_EXISTS = new ErrorCode(2_003_000, "问卷结果不存在");
    ErrorCode QUESTIONNAIRE_RESULT_ACCESS_DENIED = new ErrorCode(2_003_001, "无权限访问该问卷结果");
    ErrorCode QUESTIONNAIRE_RESULT_ALREADY_COMPLETED = new ErrorCode(2_003_002, "问卷已完成，无需重复提交");
    ErrorCode QUESTIONNAIRE_RESULT_GENERATION_FAILED = new ErrorCode(2_003_003, "问卷结果生成失败");
    ErrorCode QUESTIONNAIRE_RESULT_INVALID_STATUS = new ErrorCode(2_003_004, "问卷结果状态无效");
    ErrorCode QUESTIONNAIRE_RESULT_RETAKE_TOO_SOON = new ErrorCode(2_003_005, "距离上次测试时间过短，暂不支持重测");

    // ========== 问卷答案提交相关 2-003-100 ==========
    ErrorCode QUESTIONNAIRE_ANSWER_INVALID = new ErrorCode(2_003_100, "问卷答案格式无效");
    ErrorCode QUESTIONNAIRE_ANSWER_INCOMPLETE = new ErrorCode(2_003_101, "问卷答案不完整");
    ErrorCode QUESTIONNAIRE_ANSWER_SUBMIT_FAILED = new ErrorCode(2_003_102, "问卷答案提交失败");

}