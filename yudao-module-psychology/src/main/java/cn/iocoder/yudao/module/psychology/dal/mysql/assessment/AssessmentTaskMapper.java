package cn.iocoder.yudao.module.psychology.dal.mysql.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentTaskPageReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AssessmentTaskMapper extends BaseMapperX<AssessmentTaskDO> {

    default PageResult<AssessmentTaskDO> selectPage(AssessmentTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AssessmentTaskDO>()
                .likeIfPresent(AssessmentTaskDO::getTaskNo, reqVO.getTaskNo())
                .likeIfPresent(AssessmentTaskDO::getName, reqVO.getName())
                .eqIfPresent(AssessmentTaskDO::getScaleCode, reqVO.getScaleCode())
                .eqIfPresent(AssessmentTaskDO::getTargetAudience, reqVO.getTargetAudience())
                .eqIfPresent(AssessmentTaskDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AssessmentTaskDO::getPublishUserId, reqVO.getPublishUserId())
                .betweenIfPresent(AssessmentTaskDO::getDeadline, reqVO.getDeadline())
                .betweenIfPresent(AssessmentTaskDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AssessmentTaskDO::getId));
    }

    default AssessmentTaskDO selectByTaskNo(String taskNo) {
        return selectOne(AssessmentTaskDO::getTaskNo, taskNo);
    }

}



