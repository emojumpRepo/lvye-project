package cn.iocoder.yudao.module.psychology.dal.mysql.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentScenarioSlotDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssessmentScenarioSlotMapper extends BaseMapperX<AssessmentScenarioSlotDO> {

    default java.util.List<AssessmentScenarioSlotDO> selectListByScenarioId(Long scenarioId) {
        return selectList(AssessmentScenarioSlotDO::getScenarioId, scenarioId);
    }

    /**
     * 根据问卷ID查询包含该问卷的场景插槽
     * 使用JSON函数查询包含特定问卷ID的插槽
     */
    @Select("SELECT * FROM lvye_assessment_scenario_slot WHERE questionnaire_ids LIKE CONCAT('%', #{questionnaireId}, '%') AND deleted = 0")
    List<AssessmentScenarioSlotDO> selectListByQuestionnaireId(Long questionnaireId);

    /**
     * 根据场景ID和问卷ID查询插槽
     * 查询指定场景中包含特定问卷ID的插槽
     */
    @Select("SELECT * FROM lvye_assessment_scenario_slot WHERE scenario_id = #{scenarioId} AND questionnaire_ids LIKE CONCAT('%', #{questionnaireId}, '%') AND deleted = 0")
    List<AssessmentScenarioSlotDO> selectListByScenarioIdAndQuestionnaireId(Long scenarioId, Long questionnaireId);

    /**
     * 物理删除指定场景的所有插槽
     * 使用 @InterceptorIgnore 忽略租户和逻辑删除拦截器，确保执行真正的 DELETE 语句
     * 避免软删除记录违反唯一键约束 uk_scenario_slot
     */
    @Delete("DELETE FROM lvye_assessment_scenario_slot WHERE scenario_id = #{scenarioId}")
    @InterceptorIgnore(tenantLine = "true", blockAttack = "true")
    int deletePhysicallyByScenarioId(Long scenarioId);

    default int deleteByScenarioId(Long scenarioId) {
        // 使用物理删除避免唯一键约束冲突
        // 因为软删除的记录仍然会违反 uk_scenario_slot 约束
        return deletePhysicallyByScenarioId(scenarioId);
    }
}


