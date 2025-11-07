package com.lvye.mindtrip.module.psychology.controller.app.assessment.vo;
import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.ScenarioInfoVO;
import com.lvye.mindtrip.module.psychology.controller.admin.profile.vo.StudentAssessmentQuestionnaireDetailVO;
import com.lvye.mindtrip.module.psychology.controller.app.questionnaire.vo.AppQuestionnaireAccessRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Schema(description = "App - 测评任务 Response VO")
@Data
public class WebAssessmentTaskRespVO {

    @Schema(description = "测评任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "TASK001")
    private String taskNo;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "心理健康测评")
    private String taskName;

    @Schema(description = "问卷ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> questionnaireIds;

    @Schema(description = "问卷详细信息列表")
    private List<AppQuestionnaireAccessRespVO> questionnaires;

    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "任务参与状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer participantStatus;

    @Schema(description = "场景ID", example = "1")
    private Long scenarioId;

    @Schema(description = "场景信息（当有场景ID时返回）")
    private ScenarioInfoVO scenario;

    @Schema(description = "场景明细（App专用，包含插槽及问卷访问状态）")
    private AppScenarioDetailVO scenarioDetail;

    @Schema(description = "任务完成进度，百分比0-100", example = "75")
    private Integer progress;

    @Schema(description = "是否有问卷结果正在生成", example = "false")
    private Boolean resultGenerating;

    @Schema(description = "开始时间")
    private Date startline;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "发布人", example = "张三")
    private String publishUser;

    @Schema(description = "量表名称", example = "心理健康量表")
    private String scaleName;

    @Schema(description = "问卷结果详情")
    private List<StudentAssessmentQuestionnaireDetailVO> questionnaireDetailList;


}