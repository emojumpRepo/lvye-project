package cn.iocoder.yudao.module.psychology.controller.admin.assessment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 测评结果生成
 */
@Tag(name = "管理后台 - 测评结果生成")
@RestController
@RequestMapping("/psychology/assessment-result")
@Validated
@Slf4j
public class AssessmentResultController {

    @Resource
    private AssessmentResultService assessmentResultService;

    @PostMapping("/generate")
    @Operation(summary = "生成组合测评结果")
    @PreAuthorize("@ss.hasPermission('psychology:assessment:query')")
    public CommonResult<Long> generateCombinedResult(
            @Parameter(description = "测评任务编号", required = true)
            @RequestParam @NotBlank(message = "测评任务编号不能为空") String taskNo,
            @Parameter(description = "用户ID", required = true)
            @RequestParam @NotNull(message = "用户ID不能为空") Long userId) {
        
        log.info("开始生成组合测评结果，taskNo={}, userId={}", taskNo, userId);
        
        try {
            Long resultId = assessmentResultService.generateAndSaveCombinedResult(taskNo, userId);
            
            if (resultId != null) {
                log.info("组合测评结果生成成功，resultId={}", resultId);
                return success(resultId);
            } else {
                log.warn("组合测评结果生成失败，可能是没有找到问卷结果数据");
                return success(null);
            }
        } catch (Exception e) {
            log.error("生成组合测评结果时发生异常，taskNo={}, userId={}", taskNo, userId, e);
            throw e;
        }
    }


}
