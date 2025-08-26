package cn.iocoder.yudao.module.psychology.controller.app.assessment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentQuestionnaireDetailVO;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentTaskRespVO;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentTaskVO;
import cn.iocoder.yudao.module.psychology.controller.app.questionnaire.vo.AppQuestionnaireAccessRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.QuestionnaireRespVO;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentParticipantService;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentTaskService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireAccessService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "学生家长端 - 测评任务")
@RestController
@RequestMapping("/psychology/assessment-task")
@Validated
public class WebAssessmentTaskController {

    @Resource
    private AssessmentTaskService assessmentTaskService;

    @Resource
    private AssessmentParticipantService assessmentParticipantService;

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private QuestionnaireResultService questionnaireResultService;

    @Resource
    private QuestionnaireAccessService questionnaireAccessService;

    @GetMapping("/my-tasks")
    @Operation(summary = "获得我的测评任务列表")
    @DataPermission(enable = false)
    public CommonResult<List<WebAssessmentTaskRespVO>> getMyAssessmentTasks() {
        Long userId = WebFrameworkUtils.getLoginUserId();
        List<WebAssessmentTaskVO> list = assessmentTaskService.selectListByUserId();
        List<WebAssessmentTaskRespVO> respList = BeanUtils.toBean(list, WebAssessmentTaskRespVO.class);
        if (respList != null && !respList.isEmpty()) {
            for (WebAssessmentTaskRespVO resp : respList) {
                Integer participantStatus = assessmentParticipantService.getParticipantStatus(resp.getTaskNo(), userId);
                resp.setParticipantStatus(participantStatus);

                // 计算完成进度：已完成问卷数 / 总问卷数 * 100
                if (resp.getQuestionnaireIds() != null && !resp.getQuestionnaireIds().isEmpty()) {
                    int total = resp.getQuestionnaireIds().size();
                    int completed = 0;
                    for (Long questionnaireId : resp.getQuestionnaireIds()) {
                        if (questionnaireResultService.hasUserCompletedTaskQuestionnaire(resp.getTaskNo(), questionnaireId, userId)) {
                            completed++;
                        }
                    }
                    int progress = total == 0 ? 0 : (int) Math.round((completed * 100.0) / total);
                    resp.setProgress(progress);
                } else {
                    resp.setProgress(0);
                }
            }
        }
        return success(respList);
    }

    @GetMapping("/get")
    @Operation(summary = "获得测评任务详情")
    @Parameter(name = "taskNo", description = "任务编号", required = true, example = "1024")
    @DataPermission(enable = false)
    public CommonResult<WebAssessmentTaskRespVO> getAssessmentTask(@RequestParam String taskNo) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        AssessmentTaskDO assessmentTask = assessmentTaskService.getAssessmentTask(taskNo);
        WebAssessmentTaskRespVO respVO = BeanUtils.toBean(assessmentTask, WebAssessmentTaskRespVO.class);

        // 填充问卷详细信息
        if (assessmentTask.getQuestionnaireIds() != null && !assessmentTask.getQuestionnaireIds().isEmpty()) {
            List<AppQuestionnaireAccessRespVO> questionnaireInfos = assessmentTask.getQuestionnaireIds().stream()
                    .map(questionnaireId -> {
                        QuestionnaireRespVO questionnaire = questionnaireService.getQuestionnaire(questionnaireId);
                        if (questionnaire != null) {
                            AppQuestionnaireAccessRespVO questionnaireAccessRespVO = new AppQuestionnaireAccessRespVO();
                            boolean completed = questionnaireResultService.hasUserCompletedTaskQuestionnaire(taskNo, questionnaire.getId(), userId);
                            boolean accessible = questionnaireAccessService.checkQuestionnaireAccess(questionnaire.getId(), userId);
                            questionnaireAccessRespVO.setQuestionnaireId(questionnaire.getId());
                            questionnaireAccessRespVO.setExternalLink(questionnaire.getExternalLink());
                            questionnaireAccessRespVO.setQuestionnaireTitle(questionnaire.getTitle());
                            questionnaireAccessRespVO.setDescription(questionnaire.getDescription());
                            questionnaireAccessRespVO.setQuestionnaireType(questionnaire.getQuestionnaireType());
                            questionnaireAccessRespVO.setQuestionCount(questionnaire.getQuestionCount());
                            questionnaireAccessRespVO.setEstimatedDuration(questionnaire.getEstimatedDuration());
                            questionnaireAccessRespVO.setCompleted(completed);
                            questionnaireAccessRespVO.setAccessible(accessible);
                            return questionnaireAccessRespVO;
                        }
                        return null;
                    })
                    .filter(info -> info != null)
                    .collect(Collectors.toList());
            respVO.setQuestionnaires(questionnaireInfos);
        }
        //补充问卷答案
        List<StudentAssessmentQuestionnaireDetailVO> resultList = questionnaireResultService.selectQuestionnaireResultByTaskNoAndUserId(taskNo, userId);
        respVO.setQuestionnaireDetailList(resultList);
        return success(respVO);
    }

    @GetMapping("/questionnaire-result-detail")
    @Operation(summary = "获得测评问卷回答详情")
    @DataPermission(enable = false)
    public CommonResult<StudentAssessmentQuestionnaireDetailVO> getAssessmentTaskQuestionnaireDetail(@RequestParam String taskNo
            , @RequestParam Long questionnaireId) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        StudentAssessmentQuestionnaireDetailVO questionnaireDetail = questionnaireResultService.selectQuestionnaireResultByUnique(taskNo, questionnaireId, userId);
        return success(questionnaireDetail);
    }

}