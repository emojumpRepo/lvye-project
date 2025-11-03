package cn.iocoder.yudao.module.psychology.dal.mysql.intervention;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.intervention.InterventionTemplateStepDO;
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

}
