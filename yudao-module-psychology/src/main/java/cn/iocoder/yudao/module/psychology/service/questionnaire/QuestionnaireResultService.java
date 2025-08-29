package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentQuestionnaireDetailVO;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.QuestionnaireResultRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 问卷结果服务接口
 *
 * @author 芋道源码
 */
public interface QuestionnaireResultService {

    /**
     * 检查用户是否已完成对应测评任务的问卷
     *
     * @param taskNo 任务编号
     * @param questionnaireId 问卷ID
     * @param userId 用户ID
     * @return 是否已完成
     */
    boolean hasUserCompletedTaskQuestionnaire(String taskNo, Long questionnaireId, Long userId);

    /**
     * 检查用户是否已完成问卷
     *
     * @param questionnaireId 问卷ID
     * @param userId 用户ID
     * @return 是否已完成
     */
    boolean hasUserCompletedQuestionnaire(Long questionnaireId, Long userId);

    /**
     * 根据测评任务编号和用户ID查询问卷结果
     * @param taskNo
     * @param userId
     * @return
     */
    List<StudentAssessmentQuestionnaireDetailVO> selectQuestionnaireResultByTaskNoAndUserId(String taskNo, Long userId);

    /**
     * 根据测评任务编号,问卷ID,用户ID查询问卷结果
     * @param taskNo
     * @param questionnaireId
     * @param userId
     * @return
     */
    StudentAssessmentQuestionnaireDetailVO selectQuestionnaireResultByUnique(String taskNo, Long questionnaireId, Long userId);

    /**
     * 根据ID获取问卷结果
     *
     * @param id 问卷结果ID
     * @return 问卷结果详情
     */
    QuestionnaireResultRespVO getQuestionnaireResult(Long id);

}