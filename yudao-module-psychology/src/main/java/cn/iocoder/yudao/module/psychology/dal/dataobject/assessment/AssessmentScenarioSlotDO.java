package cn.iocoder.yudao.module.psychology.dal.dataobject.assessment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@TableName(value = "lvye_assessment_scenario_slot", autoResultMap = true)
@TenantIgnore  // 测评场景插槽表为全局配置表，不进行租户隔离
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentScenarioSlotDO extends BaseDO {

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
     * 关联问卷ID列表（JSON格式存储，如：[1,2,3]）
     */
    private String questionnaireIds;

    /**
     * 关联问卷ID列表（非持久化字段，用于业务逻辑处理）
     */
    @TableField(exist = false)
    private List<Long> questionnaireIdList;
}


