package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentQuestionnaireDetailVO;
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

}