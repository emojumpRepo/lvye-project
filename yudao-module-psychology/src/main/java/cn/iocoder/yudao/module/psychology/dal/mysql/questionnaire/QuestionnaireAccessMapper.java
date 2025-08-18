package cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireAccessDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问卷访问记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface QuestionnaireAccessMapper extends BaseMapperX<QuestionnaireAccessDO> {

    /**
     * 分页查询访问记录
     */
    default PageResult<QuestionnaireAccessDO> selectPage(Object reqVO) {
        // TODO: 待创建QuestionnaireAccessPageReqVO后替换Object类型
        return selectPage((cn.iocoder.yudao.framework.common.pojo.PageParam) reqVO, 
                new LambdaQueryWrapperX<QuestionnaireAccessDO>()
                .orderByDesc(QuestionnaireAccessDO::getAccessTime));
    }

    /**
     * 根据问卷ID和学生档案ID查询访问记录
     */
    default List<QuestionnaireAccessDO> selectListByQuestionnaireAndStudent(Long questionnaireId, Long studentProfileId) {
        return selectList(new LambdaQueryWrapperX<QuestionnaireAccessDO>()
                .eq(QuestionnaireAccessDO::getQuestionnaireId, questionnaireId)
                .eq(QuestionnaireAccessDO::getStudentProfileId, studentProfileId)
                .orderByDesc(QuestionnaireAccessDO::getAccessTime));
    }

    /**
     * 统计问卷访问次数
     */
    default Long countByQuestionnaireId(Long questionnaireId) {
        return selectCount(QuestionnaireAccessDO::getQuestionnaireId, questionnaireId);
    }

    /**
     * 统计学生访问次数
     */
    default Long countByStudentProfileId(Long studentProfileId) {
        return selectCount(QuestionnaireAccessDO::getStudentProfileId, studentProfileId);
    }

    /**
     * 统计指定时间范围内的访问次数
     */
    default Long countByTimeRange(Long questionnaireId, LocalDateTime startTime, LocalDateTime endTime) {
        return selectCount(new LambdaQueryWrapperX<QuestionnaireAccessDO>()
                .eqIfPresent(QuestionnaireAccessDO::getQuestionnaireId, questionnaireId)
                .between(QuestionnaireAccessDO::getAccessTime, startTime, endTime));
    }

    /**
     * 查询最近的访问记录
     */
    default QuestionnaireAccessDO selectLatestByQuestionnaireAndStudent(Long questionnaireId, Long studentProfileId) {
        return selectOne(new LambdaQueryWrapperX<QuestionnaireAccessDO>()
                .eq(QuestionnaireAccessDO::getQuestionnaireId, questionnaireId)
                .eq(QuestionnaireAccessDO::getStudentProfileId, studentProfileId)
                .orderByDesc(QuestionnaireAccessDO::getAccessTime)
                .last("LIMIT 1"));
    }

}