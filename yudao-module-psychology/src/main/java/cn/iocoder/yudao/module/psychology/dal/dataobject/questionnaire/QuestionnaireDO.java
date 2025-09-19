package cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 心理问卷 DO
 *
 * @author 芋道源码
 */
@TableName(value = "lvye_questionnaire", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionnaireDO extends TenantBaseDO {

    /**
     * 问卷ID
     */
    @TableId
    private Long id;

    /**
     * 问卷标题
     */
    private String title;

    /**
     * 问卷描述
     */
    private String description;

    /**
     * 问卷类型：1-心理健康，2-学习适应，3-人际关系，4-情绪管理
     */
    private Integer questionnaireType;

    /**
     * 目标对象：1-学生，2-家长
     */
    private Integer targetAudience;

    /**
     * 测评维度（多选，逗号分隔字典键值）
     */
    private String assessmentDimension;

    /**
     * 外部系统问卷ID
     */
    private String externalId;

    /**
     * 外部问卷链接
     */
    private String externalLink;

    /**
     * 问卷编码（外部系统提供的编码）
     */
    private String surveyCode;

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 预计用时（分钟）
     */
    private Integer estimatedDuration;

    /**
     * 问卷内容（题目、选项等）
     */
    private String content;

    /**
     * 评分规则配置
     */
    private String scoringRules;

    /**
     * 结果报告模板
     */
    private String resultTemplate;

    /**
     * 状态：0-草稿，1-已发布，2-已暂停，3-已关闭
     */
    private Integer status;

    /**
     * 是否启用：0-否，1-是
     */
    private Integer isOpen;

    /**
     * 是否支持独立使用：0-否，1-是（对应数据库字段 is_open）
     */
    private Integer supportIndependentUse;

    /**
     * 访问次数
     */
    private Integer accessCount;

    /**
     * 完成次数
     */
    private Integer completionCount;

    /**
     * 同步状态：0-未同步，1-已同步，2-同步失败
     */
    private Integer syncStatus;

    /**
     * 最后同步时间
     */
    private LocalDateTime lastSyncTime;

}