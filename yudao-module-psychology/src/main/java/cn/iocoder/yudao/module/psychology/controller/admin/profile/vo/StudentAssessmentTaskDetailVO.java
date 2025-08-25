package cn.iocoder.yudao.module.psychology.controller.admin.profile.vo;

import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskQuestionnaireDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-25
 * @Description:学生测评任务详情
 * @Version: 1.0
 */
@Data
public class StudentAssessmentTaskDetailVO {

    /**
     * 任务ID（唯一）
     */
    @Schema(description = "任务ID")
    private String taskId;


    /**
     * 任务编号（唯一）
     */
    @Schema(description = "任务编号")
    private String taskNo;

    /**
     * 任务名称
     */
    @Schema(description = "任务名称")
    private String taskName;

    /**
     *目标对象（字典：target_audience）
     */
    @Schema(description = "目标对象")
    private Integer targetAudience;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private Date startline;

    /**
     * 截止时间
     */
    @Schema(description = "截止时间")
    private Date deadline;

    /**
     * 问卷详情
     */
    @Schema(description = "问卷详情")
    private List<AssessmentTaskQuestionnaireDO> questionnaireList;

    /**
     * 问卷结果详情
     */
    @Schema(description = "问卷结果详情")
    private List<StudentAssessmentQuestionnaireDetailVO> questionnaireDetailList;


}
