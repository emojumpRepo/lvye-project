package com.lvye.mindtrip.module.psychology.dal.mysql.assessment;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.AssessmentTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-11
 * @Description:试题模板数据库组件
 * @Version: 1.0
 */
@Mapper
public interface AssessmentTemplateMapper extends BaseMapperX<AssessmentTemplateDO> {

    default List<AssessmentTemplateDO> selectTempalteList() {
        return selectList();
    }


}
