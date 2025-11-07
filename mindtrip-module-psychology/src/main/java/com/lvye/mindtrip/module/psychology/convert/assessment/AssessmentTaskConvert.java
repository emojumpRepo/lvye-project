package com.lvye.mindtrip.module.psychology.convert.assessment;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.AssessmentTaskRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.AssessmentTaskSaveReqVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 测评任务 Convert
 */
@Mapper
public interface AssessmentTaskConvert {

    AssessmentTaskConvert INSTANCE = Mappers.getMapper(AssessmentTaskConvert.class);

    AssessmentTaskDO convert(AssessmentTaskSaveReqVO bean);

    AssessmentTaskRespVO convert(AssessmentTaskDO bean);

    List<AssessmentTaskRespVO> convertList(List<AssessmentTaskDO> list);

    PageResult<AssessmentTaskRespVO> convertPage(PageResult<AssessmentTaskDO> page);

}