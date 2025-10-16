package cn.iocoder.yudao.module.psychology.controller.app.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.psychology.controller.app.questionnaire.vo.*;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.QuestionnaireRespVO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireAccessService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireResultService;
import cn.iocoder.yudao.module.psychology.service.questionnaire.QuestionnaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
// import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

@Tag(name = "学生端 - 问卷访问管理")
@RestController
@RequestMapping("/psychology/app/questionnaire")
@Validated
@Slf4j
public class AppQuestionnaireController {

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private QuestionnaireAccessService questionnaireAccessService;

    @Resource
    private QuestionnaireResultService questionnaireResultService;

    @GetMapping("/available-list")
    @Operation(summary = "获取可用问卷列表（带完成与可访问标记）")
    // @PreAuthenticated
    public CommonResult<List<Map<String, Object>>> getAvailableQuestionnaireList(
            @RequestParam("userId") Long userId,
            @RequestParam("taskNo")  String taskNo,
            @RequestParam(value = "targetAudience", required = false) Integer targetAudience) {

        // 获取所有问卷列表，然后根据 targetAudience 进行筛选
        List<QuestionnaireRespVO> allQuestionnaires = questionnaireService.getAllQuestionnaireList();
        List<QuestionnaireRespVO> list = new ArrayList<>();

        // 如果指定了目标受众，进行筛选
        if (targetAudience != null) {
            for (QuestionnaireRespVO q : allQuestionnaires) {
                if (targetAudience.equals(q.getTargetAudience())) {
                    list.add(q);
                }
            }
        } else {
            list = allQuestionnaires;
        }

        List<Map<String, Object>> resp = new ArrayList<>();
        for (QuestionnaireRespVO q : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", q.getId());
            item.put("title", q.getTitle());
            item.put("description", q.getDescription());
            boolean completed = questionnaireResultService.hasUserCompletedTaskQuestionnaire(taskNo, q.getId(), userId);
            boolean accessible = questionnaireAccessService.checkQuestionnaireAccess(q.getId(), userId);
            item.put("completed", completed);
            item.put("accessible", accessible);
            resp.add(item);
        }
        return success(resp);
    }

    @GetMapping("/get")
    @Operation(summary = "获取问卷详情（带状态标记）")
    // @PreAuthenticated
    public CommonResult<Map<String, Object>> getQuestionnaire(
            @RequestParam("id") Long id,
            @RequestParam("taskNo")  String taskNo,
            @RequestParam("userId") Long userId) {

        QuestionnaireRespVO questionnaire = questionnaireService.getQuestionnaire(id);
        if (questionnaire == null) {
            return success(new HashMap<>());
        }
        boolean completed = questionnaireResultService.hasUserCompletedTaskQuestionnaire(taskNo, id, userId);
        boolean accessible = questionnaireAccessService.checkQuestionnaireAccess(id, userId);

        Map<String, Object> data = new HashMap<>();
        data.put("id", questionnaire.getId());
        data.put("title", questionnaire.getTitle());
        data.put("description", questionnaire.getDescription());
        data.put("completed", completed);
        data.put("accessible", accessible);
        return success(data);
    }

    @PostMapping("/access")
    @Operation(summary = "记录问卷访问")
    // @PreAuthenticated
    // @OperateLog(type = CREATE)
    public CommonResult<AppQuestionnaireAccessRespVO> accessQuestionnaire(
            @Valid @RequestBody AppQuestionnaireAccessReqVO accessReqVO) {

        // 权限检查
        boolean accessible = questionnaireAccessService.checkQuestionnaireAccess(
                accessReqVO.getQuestionnaireId(), accessReqVO.getUserId());
        AppQuestionnaireAccessRespVO respVO = new AppQuestionnaireAccessRespVO();
        if (!accessible) {
            respVO.setAccessible(false);
            respVO.setStatusMessage("无权限访问该问卷");
            return success(respVO);
        }

        // 问卷存在性检查
        QuestionnaireRespVO questionnaire = questionnaireService.getQuestionnaire(accessReqVO.getQuestionnaireId());
        if (questionnaire == null) {
            respVO.setAccessible(false);
            respVO.setStatusMessage("问卷不存在");
            return success(respVO);
        }

        String clientIp = accessReqVO.getAccessIp() != null ? accessReqVO.getAccessIp() : "127.0.0.1";
        Long accessId = questionnaireAccessService.recordQuestionnaireAccess(
                accessReqVO.getQuestionnaireId(),
                accessReqVO.getUserId(),
                clientIp,
                accessReqVO.getUserAgent(),
                accessReqVO.getAccessSource()
        );

        respVO.setAccessible(true);
        respVO.setStatusMessage("访问成功");
        respVO.setAccessId(accessId);
        respVO.setQuestionnaireId(accessReqVO.getQuestionnaireId());
        respVO.setAccessTime(LocalDateTime.now());
        return success(respVO);
    }

    @PostMapping("/update-session")
    @Operation(summary = "更新问卷会话时长（秒）")
    // @PreAuthenticated
    // @OperateLog(type = UPDATE)
    public CommonResult<Boolean> updateSessionDuration(
            @RequestParam("accessId") Long accessId,
            @RequestParam("sessionDuration") Integer sessionDuration) {
        questionnaireAccessService.updateSessionDuration(accessId, sessionDuration);
        return success(true);
    }

    @GetMapping("/completion-check")
    @Operation(summary = "检查问卷是否已完成")
    // @PreAuthenticated
    public CommonResult<Boolean> checkQuestionnaireCompletion(
            @RequestParam("questionnaireId") Long questionnaireId,
            @RequestParam("taskNo") String taskNo,
            @RequestParam("userId") Long userId) {
        boolean completed = questionnaireResultService.hasUserCompletedTaskQuestionnaire(taskNo, questionnaireId, userId);
        return success(completed);
    }

    @GetMapping("/access-check")
    @Operation(summary = "检查问卷是否可访问")
    // @PreAuthenticated
    public CommonResult<Boolean> checkQuestionnaireAccess(
            @RequestParam("questionnaireId") Long questionnaireId,
            @RequestParam("studentProfileId") Long studentProfileId) {
        boolean accessible = questionnaireAccessService.checkQuestionnaireAccess(questionnaireId, studentProfileId);
        return success(accessible);
    }

    @GetMapping("/recommended")
    @Operation(summary = "获取推荐问卷列表")
    // @PreAuthenticated
    public CommonResult<List<Map<String, Object>>> getRecommendedQuestionnaires(
            @RequestParam("userId") Long userId,
            @RequestParam("taskNo")  String taskNo,
            @RequestParam(value = "limit", defaultValue = "3") Integer limit) {

        // 获取所有问卷列表
        List<QuestionnaireRespVO> allQuestionnaires = questionnaireService.getAllQuestionnaireList();
        List<Map<String, Object>> resp = new ArrayList<>();

        // 限制数量并转换为流处理
        List<QuestionnaireRespVO> limitedList = new ArrayList<>();
        int count = 0;
        for (QuestionnaireRespVO q : allQuestionnaires) {
            if (count >= limit) break;
            limitedList.add(q);
            count++;
        }

        for (QuestionnaireRespVO q : limitedList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", q.getId());
            item.put("title", q.getTitle());
            boolean completed = questionnaireResultService.hasUserCompletedQuestionnaire(q.getId(), userId);
            boolean accessible = questionnaireAccessService.checkQuestionnaireAccess(q.getId(), userId);
            item.put("completed", completed);
            item.put("accessible", accessible);
            resp.add(item);
        }
        return success(resp);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取我的问卷统计")
    // @PreAuthenticated
    public CommonResult<Map<String, Object>> getMyQuestionnaireStatistics(
            @RequestParam("studentProfileId") Long studentProfileId) {

        Map<String, Object> stats = new HashMap<>();
        List<QuestionnaireRespVO> list = questionnaireService.getAllQuestionnaireList();
        int total = list.size();
        int completed = 0;
        for (QuestionnaireRespVO q : list) {
            if (questionnaireResultService.hasUserCompletedQuestionnaire(q.getId(), studentProfileId)) {
                completed++;
            }
        }
        stats.put("totalAvailable", total);
        stats.put("completed", completed);
        stats.put("pending", Math.max(0, total - completed));
        return success(stats);
    }
}


