package cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:问卷结果参数表
 * @Version: 1.0
 */
@TableName(value = "lvye_questionnaire_result_config", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionnaireResultConfigDO extends TenantBaseDO {

    /**
     * 问卷ID
     */
    @TableId
    private Long id;

    /**
     * 问卷id
     */
    private Long questionnaireId;

    /**
     * 维度结果
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
     * 计算类型
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

}
