package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentQuestionnaireDetailVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * 问卷结果服务实现（简化版本）
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class QuestionnaireResultServiceImpl implements QuestionnaireResultService {

    @Resource
    private QuestionnaireResultMapper questionnaireResultMapper;

    @Override
    public boolean hasUserCompletedTaskQuestionnaire(String taskNo, Long questionnaireId, Long userId) {
        log.debug("检查用户是否已完成问卷，任务编号: {}, 问卷ID: {}, 用户ID: {}", taskNo, questionnaireId, userId);
        
        // 使用Mapper的selectByUnique方法查询对应任务编号、userId和questionnaireId的记录
        QuestionnaireResultDO result = questionnaireResultMapper.selectByUnique(taskNo, userId, questionnaireId);
        
        // 如果存在记录且score不为null，说明用户已经完成问卷
        return result != null && result.getScore() != null;
    }

    @Override
    public boolean hasUserCompletedQuestionnaire(Long questionnaireId, Long userId) {
        log.debug("检查用户是否已完成问卷，问卷ID: {}, 用户ID: {}", questionnaireId, userId);
        Long count = questionnaireResultMapper.selectCount(
                new LambdaQueryWrapper<QuestionnaireResultDO>()
                        .eq(QuestionnaireResultDO::getQuestionnaireId, questionnaireId)
                        .eq(QuestionnaireResultDO::getUserId, userId)
        );
        return count != null && count > 0;
    }

    @Override
    public List<StudentAssessmentQuestionnaireDetailVO> selectQuestionnaireResultByTaskNoAndUserId(String taskNo, Long userId){
        List<QuestionnaireResultDO> questionnaireResultList = questionnaireResultMapper.selectListByTaskNoAndUserId(taskNo, userId);
        List<StudentAssessmentQuestionnaireDetailVO> questionnaireDetailList = new ArrayList<>();
        if (!questionnaireResultList.isEmpty()) {
            for (QuestionnaireResultDO questionnaireResultDO : questionnaireResultList) {
                StudentAssessmentQuestionnaireDetailVO questionnaireDetailVO = BeanUtils.toBean(questionnaireResultDO, StudentAssessmentQuestionnaireDetailVO.class);
                questionnaireDetailList.add(questionnaireDetailVO);
            }
        }
        return questionnaireDetailList;
    }

}
