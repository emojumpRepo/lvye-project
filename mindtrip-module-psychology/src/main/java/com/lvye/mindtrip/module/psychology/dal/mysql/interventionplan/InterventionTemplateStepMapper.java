package com.lvye.mindtrip.module.psychology.dal.mysql.interventionplan;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.interventionplan.InterventionTemplateStepDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 干预计划模板步骤 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InterventionTemplateStepMapper extends BaseMapperX<InterventionTemplateStepDO> {

    /**
     * 根据模板ID查询步骤列表
     *
     * @param templateId 模板ID
     * @return 步骤列表
     */
    default List<InterventionTemplateStepDO> selectListByTemplateId(Long templateId) {
        return selectList(InterventionTemplateStepDO::getTemplateId, templateId);
    }

    /**
     * 根据模板ID删除所有步骤
     *
     * @param templateId 模板ID
     */
    default void deleteByTemplateId(Long templateId) {
        delete(InterventionTemplateStepDO::getTemplateId, templateId);
    }

}
