package cn.iocoder.yudao.module.psychology.dal.dataobject.assessment;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName(value = "lvye_assessment_scenario", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentScenarioDO extends TenantBaseDO {

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


