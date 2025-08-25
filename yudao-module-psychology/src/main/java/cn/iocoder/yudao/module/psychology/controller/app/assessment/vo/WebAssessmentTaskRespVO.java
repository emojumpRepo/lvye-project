package cn.iocoder.yudao.module.psychology.controller.app.assessment.vo;

import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentAssessmentQuestionnaireDetailVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.QuestionnaireInfoVO;

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
    private List<QuestionnaireInfoVO> questionnaires;

    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

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