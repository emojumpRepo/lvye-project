package com.lvye.mindtrip.module.psychology.dal.dataobject.assessment;

import com.lvye.mindtrip.framework.mybatis.core.dataobject.BaseDO;
import com.lvye.mindtrip.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName(value = "lvye_assessment_scenario", autoResultMap = true)
@TenantIgnore  // 测评场景表为全局配置表，不进行租户隔离
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentScenarioDO extends BaseDO {

    @TableId
    private Long id;

    private String code;

    private String name;

    private String description;

    private Integer maxQuestionnaireCount;

    private String frontendRoute;

    private Boolean isActive;

    private String metadataJson;
}


