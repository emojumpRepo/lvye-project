package com.lvye.mindtrip.module.psychology.dal.mysql.questionnaire;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.questionnaire.ResultGenerationConfigDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 结果生成配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ResultGenerationConfigMapper extends BaseMapperX<ResultGenerationConfigDO> {

    /**
     * 分页查询配置
     */
    default PageResult<ResultGenerationConfigDO> selectPage(Object reqVO) {
        // TODO: 待创建ResultGenerationConfigPageReqVO后替换Object类型
        return selectPage((com.lvye.mindtrip.framework.common.pojo.PageParam) reqVO, 
                new LambdaQueryWrapperX<ResultGenerationConfigDO>()
                .orderByDesc(ResultGenerationConfigDO::getId));
    }

    /**
     * 根据配置名称和版本查询配置
     */
    default ResultGenerationConfigDO selectByConfigNameAndVersion(String configName, String version) {
        return selectOne(new LambdaQueryWrapperX<ResultGenerationConfigDO>()
                .eq(ResultGenerationConfigDO::getConfigName, configName)
                .eq(ResultGenerationConfigDO::getVersion, version));
    }

    /**
     * 根据问卷ID查询激活的配置
     */
    default ResultGenerationConfigDO selectActiveByQuestionnaireId(Long questionnaireId) {
        LocalDateTime now = LocalDateTime.now();
        return selectOne(new LambdaQueryWrapperX<ResultGenerationConfigDO>()
                .eq(ResultGenerationConfigDO::getQuestionnaireId, questionnaireId)
                .eq(ResultGenerationConfigDO::getConfigType, 1) // 单问卷结果
                .eq(ResultGenerationConfigDO::getIsActive, 1)
                .le(ResultGenerationConfigDO::getEffectiveTime, now)
                .and(wrapper -> wrapper.isNull(ResultGenerationConfigDO::getExpireTime)
                        .or().gt(ResultGenerationConfigDO::getExpireTime, now))
                .orderByDesc(ResultGenerationConfigDO::getEffectiveTime)
                .last("LIMIT 1"));
    }

    /**
     * 根据测评模板ID查询激活的配置
     */
    default ResultGenerationConfigDO selectActiveByAssessmentTemplateId(Long assessmentTemplateId) {
        LocalDateTime now = LocalDateTime.now();
        return selectOne(new LambdaQueryWrapperX<ResultGenerationConfigDO>()
                .eq(ResultGenerationConfigDO::getAssessmentTemplateId, assessmentTemplateId)
                .eq(ResultGenerationConfigDO::getConfigType, 2) // 组合测评结果
                .eq(ResultGenerationConfigDO::getIsActive, 1)
                .le(ResultGenerationConfigDO::getEffectiveTime, now)
                .and(wrapper -> wrapper.isNull(ResultGenerationConfigDO::getExpireTime)
                        .or().gt(ResultGenerationConfigDO::getExpireTime, now))
                .orderByDesc(ResultGenerationConfigDO::getEffectiveTime)
                .last("LIMIT 1"));
    }

    /**
     * 查询激活的配置列表
     */
    default List<ResultGenerationConfigDO> selectActiveConfigs(Integer configType) {
        LocalDateTime now = LocalDateTime.now();
        return selectList(new LambdaQueryWrapperX<ResultGenerationConfigDO>()
                .eqIfPresent(ResultGenerationConfigDO::getConfigType, configType)
                .eq(ResultGenerationConfigDO::getIsActive, 1)
                .le(ResultGenerationConfigDO::getEffectiveTime, now)
                .and(wrapper -> wrapper.isNull(ResultGenerationConfigDO::getExpireTime)
                        .or().gt(ResultGenerationConfigDO::getExpireTime, now))
                .orderByDesc(ResultGenerationConfigDO::getEffectiveTime));
    }

    /**
     * 停用指定配置名称的其他版本
     */
    default void deactivateOtherVersions(String configName, String currentVersion) {
        update(null, new LambdaUpdateWrapper<ResultGenerationConfigDO>()
                .eq(ResultGenerationConfigDO::getConfigName, configName)
                .ne(ResultGenerationConfigDO::getVersion, currentVersion)
                .set(ResultGenerationConfigDO::getIsActive, 0));
    }

}