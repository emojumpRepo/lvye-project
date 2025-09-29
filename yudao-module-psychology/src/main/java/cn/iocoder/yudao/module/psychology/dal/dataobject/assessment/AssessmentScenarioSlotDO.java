package cn.iocoder.yudao.module.psychology.dal.dataobject.assessment;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName(value = "lvye_assessment_scenario_slot", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentScenarioSlotDO extends TenantBaseDO {

    @TableId
    private Long id;

    private Long scenarioId;

    private String slotKey;

    private String slotName;

    private Integer slotOrder;

    private String allowedQuestionnaireTypes;

    private String frontendComponent;

    /**
     * 元数据JSON，用于存储插槽的额外配置信息
     */
    private String metadataJson;

    /**
     * 关联问卷ID（lvye_questionnaire.id）
     */
    private Long questionnaireId;
}


