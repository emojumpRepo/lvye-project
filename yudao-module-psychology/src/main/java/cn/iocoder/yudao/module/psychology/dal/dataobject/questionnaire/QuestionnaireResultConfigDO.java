package cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问卷结果配置 DO
 *
 * @author MinGoo
 */
@TableName(value = "lvye_questionnaire_result_config", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionnaireResultConfigDO extends TenantBaseDO {

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
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
     * 题目索引
     */
    private String questionIndex;

    /**
     * 计算类型
     */
    private Integer calculateType;

    /**
     * 计算公式
     */
    private String calculateFormula;

    /**
     * 教师端评语
     */
    private String teacherComment;

    /**
     * 学生端评语
     */
    private String studentComment;

    /**
     * 是否异常
     */
    private Integer isAbnormal;

    /**
     * 等级：优秀、良好、一般、较差、很差
     */
    private String level;

    /**
     * 描述
     */
    private String description;

}
