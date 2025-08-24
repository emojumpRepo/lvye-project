package cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 结果生成配置 DO
 *
 * @author 芋道源码
 */
@TableName(value = "lvye_result_generation_config", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class ResultGenerationConfigDO extends TenantBaseDO {

    /**
     * 配置ID
     */
    @TableId
    private Long id;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置类型：1-单问卷结果，2-组合测评结果
     */
    private Integer configType;

    /**
     * 问卷ID（单问卷配置）
     */
    private Long questionnaireId;

    /**
     * 测评模板ID（组合配置）
     */
    private Long assessmentTemplateId;

    /**
     * 配置版本
     */
    private String version;

    /**
     * 评分算法配置
     */
    private String scoringAlgorithm;

    /**
     * 风险等级判定规则
     */
    private String riskLevelRules;

    /**
     * 权重配置（组合测评用）
     */
    private String weightConfig;

    /**
     * 报告模板配置
     */
    private String reportTemplate;

    /**
     * 是否激活：1-激活，0-停用
     */
    private Integer isActive;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

}