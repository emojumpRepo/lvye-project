package cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问卷维度 DO
 */
@TableName(value = "lvye_questionnaire_dimension", autoResultMap = true)
@TenantIgnore  // 问卷维度表为全局配置表，不进行租户隔离
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionnaireDimensionDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 问卷ID
     */
    private Long questionnaireId;

    /**
     * 维度名称
     */
    private String dimensionName;

    /**
     * 维度编码
     */
    private String dimensionCode;

    /**
     * 描述
     */
    private String description;

    /**
     * 兼容旧类型，可为空
     */
    private Integer calculateType;

    /**
     * 是否参与模块计算（0：否，1：是）
     */
    private Integer participateModuleCalc;

    /**
     * 是否参与测评计算（0：否，1：是）
     */
    private Integer participateAssessmentCalc;

    /**
     * 是否参与心理问题排行（0：否，1：是）
     */
    private Integer participateRanking;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态（0：禁用，1：启用）
     */
    private Integer status;
}


