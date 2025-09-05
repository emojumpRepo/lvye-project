package cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 风险等级干预建议VO
 *
 * @author 芋道源码
 */
@Schema(description = "风险等级干预建议")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskLevelInterventionVO {

    @Schema(description = "风险等级", example = "1")
    private Integer riskLevel;

    @Schema(description = "风险等级名称", example = "无/低风险")
    private String riskLevelName;

    @Schema(description = "分级标准", example = "心理健康状况PHCSS量表6个维度得分均高于界值")
    private String criteria;

    @Schema(description = "评价", example = "目前的心理状况良好。")
    private String evaluation;

    @Schema(description = "建议", example = "无需关注，可建议请继续保持良好的心境。")
    private String suggestion;

    @Schema(description = "是否为当前风险等级", example = "true")
    private Boolean isCurrent;

    @Schema(description = "优先级", example = "1")
    private Integer priority;

    /**
     * 获取预定义的四个风险等级干预建议
     */
    public static List<RiskLevelInterventionVO> getStandardInterventions() {
        return List.of(
            RiskLevelInterventionVO.builder()
                .riskLevel(1)
                .riskLevelName("无/低风险")
                .criteria("心理健康状况PHCSS量表6个维度得分均高于界值")
                .evaluation("目前的心理状况良好。")
                .suggestion("无需关注，可建议请继续保持良好的心境。")
                .priority(1)
                .build(),
            
            RiskLevelInterventionVO.builder()
                .riskLevel(2)
                .riskLevelName("轻度风险")
                .criteria("心理健康状况PHCSS量表有1~2个维度得分低于界值")
                .evaluation("目前为心理健康问题的低风险人群。")
                .suggestion("日常关注，可建议多做运动，参加社交活动，丰富日常生活。")
                .priority(2)
                .build(),
            
            RiskLevelInterventionVO.builder()
                .riskLevel(3)
                .riskLevelName("中度风险")
                .criteria("心理健康状况PHCSS量表有3个维度得分低于界值")
                .evaluation("目前为心理健康问题的中风险人群。")
                .suggestion("建议多与家长、老师、同伴交流自己的感受、想法，寻求他们的帮助和情感支持；多参与户外活动，释放压力、调节情绪；关注学校心理咨询中心的活动，必要时寻求心理咨询帮助【补充说明：鉴于小学生正处于身心快速发展时期，此结果仅作部分参考，建议同时关注家长端评估结果】")
                .priority(3)
                .build(),
            
            RiskLevelInterventionVO.builder()
                .riskLevel(4)
                .riskLevelName("重度风险")
                .criteria("心理健康状况PHCSS量表有大于等于4个维度得分低于界值")
                .evaluation("目前为心理健康问题的高风险人群。")
                .suggestion("建议积极寻求家长、学校老师及学校心理咨询中心的帮助，必要时在家长陪同下至专科门诊寻求专业心理支持【鉴于小学生正处于身心快速发展时期，此结果仅作部分参考，建议同时关注家长端评估结果】")
                .priority(4)
                .build()
        );
    }
}
