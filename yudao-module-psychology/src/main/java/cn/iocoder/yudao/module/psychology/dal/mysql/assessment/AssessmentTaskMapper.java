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

    default int updateStatusByTaskNo(AssessmentTaskDO updateObj) {
        return update(new LambdaUpdateWrapper<AssessmentTaskDO>()
                .eq(AssessmentTaskDO::getTaskNo, updateObj.getTaskNo()).set(AssessmentTaskDO::getStatus, updateObj.getStatus()));
    }

    IPage<AssessmentTaskVO> selectPageList(IPage<AssessmentTaskVO> page, @Param("pageReqVO") AssessmentTaskPageReqVO pageReqVO
            , @Param("taskNos") List<String> taskNos);

    List<WebAssessmentTaskVO> selectListByUserId(@Param("userId") Long userId, @Param("isParent")Integer isParent);

}



