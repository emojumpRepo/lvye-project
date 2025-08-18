package cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 问卷结果 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuestionnaireResultRespVO extends QuestionnaireResultBaseVO {

    @Schema(description = "结果编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    @Schema(description = "问卷标题", example = "心理健康测评问卷")
    private String questionnaireTitle;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "风险等级描述", example = "中等风险")
    private String riskLevelDesc;

    @Schema(description = "生成状态描述", example = "已完成")
    private String generationStatusDesc;

}