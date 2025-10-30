package cn.iocoder.yudao.module.psychology.dal.mysql.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentTaskPageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentTaskVO;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentTaskVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssessmentTaskMapper extends BaseMapperX<AssessmentTaskDO> {

    default PageResult<AssessmentTaskDO> selectPage(AssessmentTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AssessmentTaskDO>()
                .likeIfPresent(AssessmentTaskDO::getTaskNo, reqVO.getTaskNo())
                .likeIfPresent(AssessmentTaskDO::getTaskName, reqVO.getName())
                .eqIfPresent(AssessmentTaskDO::getTargetAudience, reqVO.getTargetAudience())
                .eqIfPresent(AssessmentTaskDO::getStatus, reqVO.getStatus())
                .orderByDesc(AssessmentTaskDO::getId));
    }

    default AssessmentTaskDO selectByTaskName(String taskName) {
        return selectOne(AssessmentTaskDO::getTaskName, taskName);
    }

    default AssessmentTaskDO selectByTaskNo(String taskNo) {
        return selectOne(AssessmentTaskDO::getTaskNo, taskNo);
    }

    default AssessmentTaskDO selectLatestByEventId(Long eventId) {
        return selectOne(new LambdaQueryWrapperX<AssessmentTaskDO>()
                .eq(AssessmentTaskDO::getEventId, eventId)
                .orderByDesc(AssessmentTaskDO::getCreateTime)
                .last("LIMIT 1"));
    }

    default int updateStatusByTaskNo(AssessmentTaskDO updateObj) {
        return update(new LambdaUpdateWrapper<AssessmentTaskDO>()
                .eq(AssessmentTaskDO::getTaskNo, updateObj.getTaskNo()).set(AssessmentTaskDO::getStatus, updateObj.getStatus()));
    }

    IPage<AssessmentTaskVO> selectPageList(IPage<AssessmentTaskVO> page, @Param("pageReqVO") AssessmentTaskPageReqVO pageReqVO
            , @Param("taskNos") List<String> taskNos);

    List<WebAssessmentTaskVO> selectListByUserId(@Param("userId") Long userId, @Param("isParent")Integer isParent);

    /**
     * 分页查询正在进行的测评任务及其进度
     *
     * @param page 分页对象
     * @param pageReqVO 分页查询条件
     * @return 正在进行的任务分页列表
     */
    IPage<cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.OngoingTaskRespVO> selectOngoingTasksPage(
            IPage<cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.OngoingTaskRespVO> page,
            @Param("pageReqVO") cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.OngoingTaskPageReqVO pageReqVO);

}



