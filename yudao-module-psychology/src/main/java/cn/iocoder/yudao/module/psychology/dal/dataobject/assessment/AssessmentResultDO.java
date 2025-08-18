package cn.iocoder.yudao.module.psychology.dal.dataobject.assessment;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 测评结果 DO（维度级别与总分）
 */
@TableName(value = "lvye_assessment_result", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResultDO extends TenantBaseDO {

    /** 主键 */
    @TableId
    private Long id;

    /** 任务参与者编号 */
    private Long participantId;

    /** 维度编码（如 total 为总分，其它维度按问卷定义） */
    private String dimensionCode;

    /** 分数 */
    private Integer score;

    /** 风险等级（字典：risk_level） */
    private Integer riskLevel;

    /** 建议/结论摘要 */
    private String suggestion;

    /** 关联的问卷结果汇总 (JSON格式存储) */
    private String questionnaireResults;

    /** 综合风险等级 */
    private Integer combinedRiskLevel;

    /** 风险因素分析 (JSON格式存储) */
    private String riskFactors;

    /** 干预建议 */
    private String interventionSuggestions;

    /** 生成规则版本 */
    private String generationConfigVersion;
}



