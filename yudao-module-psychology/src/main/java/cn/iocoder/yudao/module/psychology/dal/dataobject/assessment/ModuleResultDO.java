package cn.iocoder.yudao.module.psychology.dal.dataobject.assessment;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 模块结果 DO
 *
 * @author MinGoo
 */
@TableName(value = "lvye_module_result", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class ModuleResultDO extends TenantBaseDO {

    /**
     * 结果ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 测评结果ID
     */
    private Long assessmentResultId;

    /**
     * 场景插槽ID
     */
    private Long scenarioSlotId;

    /**
     * 插槽键
     */
    private String slotKey;

    /**
     * 模块分数
     */
    private BigDecimal moduleScore;

    /**
     * 风险等级（1-无/低风险，2-轻度风险，3-中度风险，4-重度风险）
     */
    private Integer riskLevel;

    /**
     * 教师评语
     */
    private String teacherComment;

    /**
     * 学生评语（从配置中随机选择的单条评语）
     */
    private String studentComment;

    /**
     * 模块描述
     */
    private String moduleDescription;
}
