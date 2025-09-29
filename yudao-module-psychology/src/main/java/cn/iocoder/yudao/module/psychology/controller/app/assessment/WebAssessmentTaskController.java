package cn.iocoder.yudao.module.psychology.controller.app.assessment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentQuestionnaireDetailVO;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentTaskRespVO;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentTaskVO;
import cn.iocoder.yudao.module.psychology.controller.app.questionnaire.vo.AppQuestionnaireAccessRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentScenarioVO;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.ScenarioQuestionnaireAccessVO;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentScenarioService;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.AppScenarioDetailVO;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.AppScenarioQuestionnaireAccessVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;

import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.QuestionnaireRespVO;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.AppQuestionnaireResultDetailVO;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentParticipantService;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentTaskService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultService;
import cn.iocoder.yudao.module.psychology.enums.ResultGenerationStatusEnum;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireAccessService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Resource
    private AssessmentScenarioService scenarioService;


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
                    // 是否有结果正在生成：查询该任务下当前用户的所有问卷结果，存在 generation_status = GENERATING 即为 true
                    List<StudentAssessmentQuestionnaireDetailVO> rs =
                            questionnaireResultService.selectQuestionnaireResultByTaskNoAndUserId(resp.getTaskNo(), userId);
                    boolean generating = false;
                    if (rs != null && !rs.isEmpty()) {
                        Integer generatingCode = ResultGenerationStatusEnum.GENERATING.getStatus();
                        for (StudentAssessmentQuestionnaireDetailVO r : rs) {
                            if (r.getGenerationStatus() != null && r.getGenerationStatus().equals(generatingCode)) {
                                generating = true;
                                break;
                            }
                        }
                    }
                    resp.setResultGenerating(generating);
                } else {
                    resp.setProgress(0);
                    resp.setResultGenerating(false);
                }
            }
        }
        return success(respList);
    }

    @GetMapping("/generating-tasks")
    @Operation(summary = "轮询获取结果正在生成中的测评任务列表")
    @DataPermission(enable = false)
    public CommonResult<List<WebAssessmentTaskRespVO>> getGeneratingTasks() {
        Long userId = WebFrameworkUtils.getLoginUserId();
        List<WebAssessmentTaskVO> list = assessmentTaskService.selectListByUserId();
        List<WebAssessmentTaskRespVO> respList = BeanUtils.toBean(list, WebAssessmentTaskRespVO.class);
        List<WebAssessmentTaskRespVO> generatingTasks = new java.util.ArrayList<>();
        if (respList != null && !respList.isEmpty()) {
            Integer generatingCode = ResultGenerationStatusEnum.GENERATING.getStatus();
            for (WebAssessmentTaskRespVO resp : respList) {
                Integer participantStatus = assessmentParticipantService.getParticipantStatus(resp.getTaskNo(), userId);
                resp.setParticipantStatus(participantStatus);

                boolean generating = false;
                boolean allCompleted = false;
                // 计算进度，同时判断是否有生成中结果
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
                    allCompleted = total > 0 && completed == total;

                    List<StudentAssessmentQuestionnaireDetailVO> rs =
                            questionnaireResultService.selectQuestionnaireResultByTaskNoAndUserId(resp.getTaskNo(), userId);
                    if (rs != null && !rs.isEmpty()) {
                        for (StudentAssessmentQuestionnaireDetailVO r : rs) {
                            if (r.getGenerationStatus() != null && r.getGenerationStatus().equals(generatingCode)) {
                                generating = true;
                                break;
                            }
                        }
                    }
                } else {
                    resp.setProgress(0);
                }

                resp.setResultGenerating(generating);
                // 仅返回“已完成全部问卷且仍在生成中”的任务
                if (generating && allCompleted) {
                    generatingTasks.add(resp);
                }
            }
        }
        return success(generatingTasks);
    }

    @GetMapping("/get")
    @Operation(summary = "获得测评任务详情")
    @Parameter(name = "taskNo", description = "任务编号", required = true, example = "1024")
    @DataPermission(enable = false)
    public CommonResult<WebAssessmentTaskRespVO> getAssessmentTask(@RequestParam String taskNo) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        AssessmentTaskDO assessmentTask = assessmentTaskService.getAssessmentTask(taskNo);
        WebAssessmentTaskRespVO respVO = BeanUtils.toBean(assessmentTask, WebAssessmentTaskRespVO.class);

        // 若绑定场景，优先返回场景详情
        if (assessmentTask.getScenarioId() != null) {
            AssessmentScenarioVO scenario = scenarioService.getScenarioQuestionnaire(assessmentTask.getScenarioId(), userId);
            // 组装 App 场景明细 VO
            AppScenarioDetailVO detail = new AppScenarioDetailVO();
            detail.setId(scenario.getId());
            detail.setCode(scenario.getCode());
            detail.setName(scenario.getName());
            detail.setMaxQuestionnaireCount(scenario.getMaxQuestionnaireCount());
            detail.setMetadataJson(scenario.getMetadataJson());
            detail.setIsActive(scenario.getIsActive());
            java.util.List<AppScenarioDetailVO.AppScenarioSlotDetailVO> slotDetails = new java.util.ArrayList<>();
            if (scenario.getSlots() != null) {
                for (AssessmentScenarioVO.ScenarioSlotVO slotVO : scenario.getSlots()) {
                    AppScenarioDetailVO.AppScenarioSlotDetailVO s = new AppScenarioDetailVO.AppScenarioSlotDetailVO();
                    s.setId(slotVO.getId());
                    s.setSlotKey(slotVO.getSlotKey());
                    s.setSlotName(slotVO.getSlotName());
                    s.setSlotOrder(slotVO.getSlotOrder());
                    s.setFrontendComponent(slotVO.getFrontendComponent());
                    s.setMetadataJson(slotVO.getMetadataJson());
                    // 映射问卷 - 处理问卷列表，取第一个问卷
                    if (slotVO.getQuestionnaires() != null && !slotVO.getQuestionnaires().isEmpty()) {
                        java.util.List<AppScenarioQuestionnaireAccessVO> list = new java.util.ArrayList<>();
                        for (ScenarioQuestionnaireAccessVO qx : slotVO.getQuestionnaires()) {
                            AppScenarioQuestionnaireAccessVO q = new AppScenarioQuestionnaireAccessVO();
                            boolean completed = questionnaireResultService.hasUserCompletedTaskQuestionnaire(taskNo, qx.getId(), userId);
                            boolean accessible = questionnaireAccessService.checkQuestionnaireAccess(qx.getId(), userId);
                            q.setId(qx.getId());
                            q.setTitle(qx.getTitle());
                            q.setDescription(qx.getDescription());
                            q.setQuestionnaireType(qx.getQuestionnaireType());
                            q.setTargetAudience(qx.getTargetAudience());
                            q.setQuestionCount(qx.getQuestionCount());
                            q.setExternalLink(qx.getExternalLink());
                            q.setEstimatedDuration(qx.getEstimatedDuration());
                            q.setStatus(qx.getStatus());
                            q.setCompleted(completed);
                            q.setAccessible(accessible);
                            // 查询问卷结果生成状态（若已提交则返回最新一条的generation_status）
                            QuestionnaireResultDO qr =
                                questionnaireResultService.getQuestionnaireResultByUnique(taskNo, qx.getId(), userId);
                            if (qr != null) {
                                q.setGenerationStatus(qr.getGenerationStatus());
                            }
                            list.add(q);
                        }
                        s.setQuestionnaires(list);
                    }
                    slotDetails.add(s);
                }
            }
            detail.setSlots(slotDetails);
            respVO.setScenarioDetail(detail);
        } else {
            // 若未绑定场景，则返回问卷详细信息
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
                        .collect(java.util.stream.Collectors.toList());
                respVO.setQuestionnaires(questionnaireInfos);
            }
        }
       
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

    @GetMapping("/my-task-results")
    @Operation(summary = "获得我的测评结果（按任务聚合问卷结果）")
    @DataPermission(enable = false)
    public CommonResult<java.util.List<AppQuestionnaireResultDetailVO>> getMyTaskResults(@RequestParam String taskNo) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        java.util.List<StudentAssessmentQuestionnaireDetailVO> list = questionnaireResultService.selectQuestionnaireResultByTaskNoAndUserId(taskNo, userId);
        java.util.List<AppQuestionnaireResultDetailVO> resp = new java.util.ArrayList<>();
        if (list != null) {
            for (StudentAssessmentQuestionnaireDetailVO src : list) {
                AppQuestionnaireResultDetailVO d = new AppQuestionnaireResultDetailVO();
                d.setId(src.getId());
                d.setQuestionnaireId(src.getQuestionnaireId());
                d.setUserId(src.getUserId());
                d.setAssessmentTaskNo(src.getAssessmentTaskNo());
                d.setAnswers(src.getAnswers());
                d.setScore(src.getScore());
                d.setRiskLevel(src.getRiskLevel());
                d.setEvaluate(src.getEvaluate());
                d.setSuggestions(src.getSuggestions());
                d.setDimensionScores(src.getDimensionScores());
                d.setResultData(src.getResultData());
                d.setCompletedTime(src.getCompletedTime());
                d.setGenerationStatus(src.getGenerationStatus());
                // 解析 JSON 字符串
                if (src.getResultData() != null && !src.getResultData().trim().isEmpty()) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        d.setResultDataParsed(mapper.readValue(src.getResultData(), Object.class));
                    } catch (Exception ignored) {
                        // 保底：解析失败时返回原始字符串
                        d.setResultDataParsed(null);
                    }
                }
                resp.add(d);
            }
        }
        return success(resp);
    }

}