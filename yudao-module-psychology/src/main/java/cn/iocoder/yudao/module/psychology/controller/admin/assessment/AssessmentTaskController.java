package cn.iocoder.yudao.module.psychology.controller.admin.assessment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.*;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentQuestionnaireDetailVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.*;
import cn.iocoder.yudao.module.psychology.dal.mysql.assessment.AssessmentTemplateMapper;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.QuestionnaireInfoVO;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentTaskService;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentScenarioService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireService;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.QuestionnaireRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 测评任务")
@RestController
@RequestMapping("/psychology/assessment-task")
@Validated
public class AssessmentTaskController {

    @Resource
    private AssessmentTemplateMapper templateMapper;

    @Resource
    private AssessmentTaskService assessmentTaskService;

    @Resource
    private AssessmentScenarioService scenarioService;

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private QuestionnaireResultService questionnaireResultService;

    @GetMapping("/get-exam-template")
    @Operation(summary = "获得测评任务")
    @DataPermission(enable = false)
    public CommonResult<List<AssessmentTemplateDO>> getAssessmentTemplate() {
        List<AssessmentTemplateDO> result = templateMapper.selectTempalteList();
        return success(result);
    }

    @PostMapping("/create")
    @Operation(summary = "创建测评任务")
    @DataPermission(enable = false)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:create')")
    public CommonResult<String> createAssessmentTask(@Valid @RequestBody AssessmentTaskSaveReqVO createReqVO) {
        return success(assessmentTaskService.createAssessmentTask(createReqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新测评任务")
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:update')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> updateAssessmentTask(@Valid @RequestBody AssessmentTaskSaveReqVO updateReqVO) {
        assessmentTaskService.updateAssessmentTask(updateReqVO);
        return success(true);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除测评任务")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:delete')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> deleteAssessmentTask(@RequestParam("taskNo") String taskNo) {
        assessmentTaskService.deleteAssessmentTask(taskNo);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得测评任务")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:query')")
    @DataPermission(enable = false)
    public CommonResult<AssessmentTaskRespVO> getAssessmentTask(@RequestParam("taskNo") String taskNo) {
        AssessmentTaskDO assessmentTask = assessmentTaskService.getAssessmentTask(taskNo);
        AssessmentTaskRespVO respVO = BeanUtils.toBean(assessmentTask, AssessmentTaskRespVO.class);

        // 填充问卷详细信息
        if (assessmentTask.getQuestionnaireIds() != null && !assessmentTask.getQuestionnaireIds().isEmpty()) {
            List<QuestionnaireInfoVO> questionnaires = new ArrayList<>();
            for (Long questionnaireId : assessmentTask.getQuestionnaireIds()) {
                QuestionnaireRespVO questionnaire = questionnaireService.getQuestionnaire(questionnaireId);
                if (questionnaire != null) {
                    QuestionnaireInfoVO info = new QuestionnaireInfoVO();
                    info.setId(questionnaire.getId());
                    info.setTitle(questionnaire.getTitle());
                    info.setDescription(questionnaire.getDescription());
                    info.setQuestionnaireType(questionnaire.getQuestionnaireType());
                    info.setTargetAudience(questionnaire.getTargetAudience());
                    info.setQuestionCount(questionnaire.getQuestionCount());
                    info.setEstimatedDuration(questionnaire.getEstimatedDuration());
                    info.setStatus(questionnaire.getStatus());
                    info.setCode(questionnaire.getSurveyCode());
                    questionnaires.add(info);
                }
            }
            respVO.setQuestionnaires(questionnaires);
        }

        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得测评任务分页")
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:query')")
    @DataPermission(enable = false)
    public CommonResult<PageResult<AssessmentTaskVO>> getAssessmentTaskPage(@Valid AssessmentTaskPageReqVO pageReqVO) {
        PageResult<AssessmentTaskVO> pageResult = assessmentTaskService.getAssessmentTaskPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AssessmentTaskVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出测评任务 Excel")
    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:export')")
    public void exportAssessmentTaskExcel(@Valid AssessmentTaskPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AssessmentTaskVO> list = assessmentTaskService.getAssessmentTaskPage(pageReqVO).getList();
        ExcelUtils.write(response, "测评任务.xls", "数据", AssessmentTaskRespVO.class,
                BeanUtils.toBean(list, AssessmentTaskRespVO.class));
    }

    @PostMapping("/publish")
    @Operation(summary = "发布测评任务")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:publish')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> publishAssessmentTask(@RequestParam("taskNo") String taskNo) {
        assessmentTaskService.publishAssessmentTask(taskNo);
        return success(true);
    }

    @PostMapping("/close")
    @Operation(summary = "关闭测评任务")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:close')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> closeAssessmentTask(@RequestParam("taskNo") String taskNo) {
        assessmentTaskService.closeAssessmentTask(taskNo);
        return success(true);
    }

    @PostMapping("/add-participants")
    @Operation(summary = "添加参与者")
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:manage-participants')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> addParticipants(@RequestBody AssessmentTaskParticipantsReqVO reqVO) {
        assessmentTaskService.addParticipants(reqVO);
        return success(true);
    }

    @PostMapping("/remove-participants")
    @Operation(summary = "移除参与者")
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:manage-participants')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> removeParticipants(@RequestBody AssessmentTaskParticipantsReqVO reqVO) {
        assessmentTaskService.removeParticipants(reqVO);
        return success(true);
    }

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "仅汇总（includeDeptTree=0）", value = "{\n  \"code\": 0,\n  \"data\": {\n    \"totalParticipants\": 123,\n    \"completedParticipants\": 100,\n    \"inProgressParticipants\": 10,\n    \"notStartedParticipants\": 13,\n    \"completionRate\": 81.30\n  },\n  \"msg\": \"\"\n}")
                    ,
                    @ExampleObject(name = "包含部门树（includeDeptTree=1）", value = "{\n  \"code\": 0,\n  \"data\": {\n    \"totalParticipants\": 123,\n    \"completedParticipants\": 100,\n    \"inProgressParticipants\": 10,\n    \"notStartedParticipants\": 13,\n    \"completionRate\": 81.30,\n    \"deptTree\": [\n      {\n        \"deptId\": 2001,\n        \"deptName\": \"高一\",\n        \"totalParticipants\": 60,\n        \"completedParticipants\": 48,\n        \"completionRate\": 80.00,\n        \"children\": [\n          {\n            \"deptId\": 30011,\n            \"deptName\": \"高一1班\",\n            \"totalParticipants\": 30,\n            \"completedParticipants\": 25,\n            \"completionRate\": 83.33\n          },\n          {\n            \"deptId\": 30012,\n            \"deptName\": \"高一2班\",\n            \"totalParticipants\": 30,\n            \"completedParticipants\": 23,\n            \"completionRate\": 76.67\n          }\n        ]\n      },\n      {\n        \"deptId\": -1,\n        \"deptName\": \"未知年级\",\n        \"totalParticipants\": 3,\n        \"completedParticipants\": 2,\n        \"completionRate\": 66.67,\n        \"children\": [\n          {\n            \"deptId\": -1,\n            \"deptName\": \"未知班级\",\n            \"totalParticipants\": 3,\n            \"completedParticipants\": 2,\n            \"completionRate\": 66.67\n          }\n        ]\n      }\n    ]\n  },\n  \"msg\": \"\"\n}")
                }
            )
        )
    })

    @GetMapping("/statistics")
    @Operation(summary = "获取任务统计信息")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
    @Parameter(name = "includeDeptTree", description = "是否返回部门树状统计：0-否（默认），1-是", required = false)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:statistics')")
    @DataPermission(enable = false)
    public CommonResult<AssessmentTaskStatisticsRespVO> getTaskStatistics(@RequestParam("taskNo") String taskNo,
                                                                         @RequestParam(value = "includeDeptTree", required = false, defaultValue = "0") Integer includeDeptTree) {
        return success(assessmentTaskService.getTaskStatistics(taskNo, includeDeptTree));
    }

    @GetMapping("/participants-list")
    @Operation(summary = "获得测评任务人员列表")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:query')")
    @DataPermission(enable = false)
    public CommonResult<List<AssessmentTaskUserVO>> getAssessmentTaskUserList(@RequestParam("taskNo") String taskNo) {
        List<AssessmentTaskUserVO> assessmentTaskUserList = assessmentTaskService.selectListByTaskNo(taskNo);
        return success(BeanUtils.toBean(assessmentTaskUserList, AssessmentTaskUserVO.class));
    }

    @PostMapping("/check-by-name")
    @Operation(summary = "检查测评任务名是否存在")
    @Parameter(name = "taskName", description = "任务名称", required = true)
//    @PreAuthorize("@ss.hasPermission('psychology:assessment-task:query')")
    @DataPermission(enable = false)
    public CommonResult<Boolean> checkTaskName(@RequestParam("taskName") String taskName) {
        assessmentTaskService.validateTaskNameUnique(null, taskName);
        return success(true);
    }

    // ========== 场景管理相关接口 ==========

    @GetMapping("/scenarios")
    @Operation(summary = "查询启用的测评场景列表")
    @DataPermission(enable = false)
    public CommonResult<List<AssessmentScenarioVO>> listScenarios() {
        List<AssessmentScenarioVO> list = scenarioService.getActiveScenarioList();
        return success(list);
    }

    @GetMapping("/scenarios/page")
    @Operation(summary = "获得测评场景分页")
    @DataPermission(enable = false)
    public CommonResult<PageResult<AssessmentScenarioDO>> getScenarioPage(@Valid AssessmentScenarioPageReqVO pageReqVO) {
        PageResult<AssessmentScenarioDO> pageResult = scenarioService.getScenarioPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/scenarios/{id}")
    @Operation(summary = "获得测评场景")
    @DataPermission(enable = false)
    public CommonResult<AssessmentScenarioDO> getScenario(@PathVariable("id") Long id) {
        AssessmentScenarioDO scenario = scenarioService.getScenario(id);
        return success(scenario);
    }

    @GetMapping("/scenarios/{id}/slots")
    @Operation(summary = "查询场景槽位定义")
    @DataPermission(enable = false)
    public CommonResult<List<AssessmentScenarioSlotDO>> listScenarioSlots(@PathVariable("id") Long scenarioId) {
        return success(scenarioService.getScenarioSlots(scenarioId));
    }

    @PostMapping("/scenarios")
    @Operation(summary = "创建场景")
    @DataPermission(enable = false)
    public CommonResult<Long> createScenario(@RequestBody @Validated AssessmentScenarioVO reqVO) {
        Long id = scenarioService.createScenario(reqVO);
        return success(id);
    }

    @PutMapping("/scenarios")
    @Operation(summary = "更新场景")
    @DataPermission(enable = false)
    public CommonResult<Boolean> updateScenario(@RequestBody @Validated AssessmentScenarioVO reqVO) {
        scenarioService.updateScenario(reqVO);
        return success(true);
    }

    @DeleteMapping("/scenarios/{id}")
    @Operation(summary = "删除场景")
    @DataPermission(enable = false)
    public CommonResult<Boolean> deleteScenario(@PathVariable("id") Long id) {
        scenarioService.deleteScenario(id);
        return success(true);
    }

    @PostMapping("/scenarios/{scenarioId}/slots")
    @Operation(summary = "批量更新场景插槽")
    @DataPermission(enable = false)
    public CommonResult<Boolean> updateScenarioSlots(
            @PathVariable("scenarioId") Long scenarioId,
            @RequestBody @Validated List<AssessmentScenarioVO.ScenarioSlotVO> slots) {
        scenarioService.updateScenarioSlots(scenarioId, slots);
        return success(true);
    }

    @GetMapping("/participants-questionnaire-page")
    @Operation(summary = "获得测评问卷人员列表")
    @DataPermission(enable = false)
    public CommonResult<PageResult<QuestionnaireUserVO>> getAssessmentTaskUserList(@Valid QuestionnaireUserPageVO pageReqVO) {
        PageResult<QuestionnaireUserVO> assessmentTaskUserList = assessmentTaskService.selectQuestionnaireUserListByTaskNoAndQuestionnaire(pageReqVO);
        return success(BeanUtils.toBean(assessmentTaskUserList, QuestionnaireUserVO.class));
    }

    @GetMapping("/participants-questionnaire-detail")
    @Operation(summary = "获得测评问卷人员回答详情")
    @DataPermission(enable = false)
    public CommonResult<StudentAssessmentQuestionnaireDetailVO> getAssessmentQuestionnaireDetail(@RequestParam String taskNo
            , @RequestParam Long questionnaireId, @RequestParam Long userId) {
        StudentAssessmentQuestionnaireDetailVO questionnaireDetail = questionnaireResultService.selectQuestionnaireResultByUnique(taskNo, questionnaireId, userId);
        return success(questionnaireDetail);
    }

    @GetMapping("/get-questionnaire-list")
    @Operation(summary = "获得测评任务问卷列表")
    @DataPermission(enable = false)
    public CommonResult<List<AssessmentTaskQuestionnaireDO>> getAssessmentQuestionnaireDetail(@RequestParam String taskNo) {
        List<AssessmentTaskQuestionnaireDO> questionnaireList = assessmentTaskService.selectQuestionnaireListByTaskNo(taskNo);
        return success(questionnaireList);
    }

    @PostMapping("/update-expire-status")
    @Operation(summary = "更新过期任务状态")
    @DataPermission(enable = false)
    public CommonResult<Boolean> updateExpireTask() {
        assessmentTaskService.updateExpireStatus();
        return success(true);
    }

    @GetMapping("/risk-level-statistics")
    @Operation(summary = "获取任务风险等级统计信息")
    @Parameter(name = "taskNo", description = "任务编号", required = true)
    @DataPermission(enable = false)
    public CommonResult<AssessmentTaskRiskLevelStatisticsVO> getTaskStatistics(@RequestParam("taskNo") String taskNo) {
        return success(assessmentTaskService.getTaskRiskStatistics(taskNo));
    }

}