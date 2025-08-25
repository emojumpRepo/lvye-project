package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 测评任务新增/修改 Request VO")
@Data
@ToString(callSuper = true)
public class AssessmentTaskSaveReqVO{

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "任务编号")
    private String taskNo;

    @Schema(description = "测评任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024年春季心理测评")
    @NotBlank(message = "测评任务名称不能为空")
    @Length(max = 120, message = "测评任务名称不能超过 120")
    private String taskName;

    @Schema(description = "目标对象 0-学生，1-家长", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "目标对象不能为空")
    private Integer targetAudience;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @NotNull(message = "开始时间不能为空")
    private Date startline;

    @Schema(description = "截止时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @NotNull(message = "截止时间不能为空")
    private Date deadline;

    @Schema(description = "年级/班级列表", example = "[1,2,3,4]")
    private List<Long> deptIdList;

    @Schema(description = "用户列表", example = "[1,2,3,4]")
    private List<Long> userIdList;

    @Schema(description = "是否立即发布任务", example = "false")
    private Boolean isPublish = false;

    @Schema(description = "问卷ID列表", example = "[1,2,3]")
    private List<Long> questionnaireIds;

    @Schema(description = "场景ID，可为空（默认NONE）", example = "1")
    private Long scenarioId;

    @Schema(description = "场景问卷分配（有槽位时使用）", example = "[{slotKey:'library',questionnaireId:1},{slotKey:'gym',questionnaireId:2}]")
    private List<SlotAssignmentVO> assignments;

    @Schema(description = "任务描述", example = "这是一个心理健康测评任务")
    @Length(max = 2000, message = "任务描述不能超过 2000 字符")
    private String description;

}