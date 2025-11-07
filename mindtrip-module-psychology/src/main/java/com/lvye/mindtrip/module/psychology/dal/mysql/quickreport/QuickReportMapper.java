package com.lvye.mindtrip.module.psychology.dal.mysql.quickreport;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.psychology.controller.admin.assessment.vo.AssessmentTaskVO;
import com.lvye.mindtrip.module.psychology.controller.admin.quickreport.vo.QuickReportPageReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.quickreport.vo.QuickReportVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.quickreport.QuickReportDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-31
 * @Description:快速上班数据层
 * @Version: 1.0
 */
@Mapper
public interface QuickReportMapper extends BaseMapperX<QuickReportDO> {

    IPage<AssessmentTaskVO> selectPageList(IPage<QuickReportVO> page, @Param("pageReqVO") QuickReportPageReqVO pageReqVO);

}
