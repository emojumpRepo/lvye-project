package cn.iocoder.yudao.module.psychology.service.quickreport.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.quickreport.vo.QuickReportPageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.quickreport.vo.QuickReportSaveReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.quickreport.vo.QuickReportHandleReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.quickreport.vo.QuickReportVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.quickreport.QuickReportDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.quickreport.QuickReportMapper;
import cn.iocoder.yudao.module.psychology.enums.QuickReportStatusEnum;
import cn.iocoder.yudao.module.psychology.enums.TimelineEventTypeEnum;
import cn.iocoder.yudao.module.psychology.service.profile.StudentTimelineService;
import cn.iocoder.yudao.module.psychology.service.quickreport.QuickReportService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-31
 * @Description:快速上报服务层
 * @Version: 1.0
 */
@Service
public class QuickReportServiceImpl implements QuickReportService {

    @Autowired
    private QuickReportMapper quickReportMapper;

    @Autowired
    private StudentTimelineService studentTimelineService;

    @Override
    public void saveQuickReport(QuickReportSaveReqVO saveReqVO){
        Long userId = WebFrameworkUtils.getLoginUserId();
        QuickReportDO quickReportDO = new QuickReportDO();
        quickReportDO.setStudentProfileId(saveReqVO.getStudentProfileId());
        quickReportDO.setReporterId(userId);
        quickReportDO.setReportTitle(saveReqVO.getReportTitle());
        quickReportDO.setReportContent(saveReqVO.getReportContent());
        quickReportDO.setUrgencyLevel(saveReqVO.getUrgencyLevel());
        quickReportDO.setIncidentTime(saveReqVO.getIncidentTime());
        quickReportDO.setReportTime(new Date());
        quickReportDO.setStatus(QuickReportStatusEnum.PENDING.getStatus());
        quickReportDO.setHandlerId(saveReqVO.getHandlerId());
        quickReportDO.setFollowUpRequired(saveReqVO.getFollowUpRequired());
        quickReportDO.setTags(saveReqVO.getTags());
        quickReportDO.setAttachments(saveReqVO.getAttachments());
        quickReportMapper.insert(quickReportDO);
        //时间线
        studentTimelineService.saveTimeline(saveReqVO.getStudentProfileId(), TimelineEventTypeEnum.QUICK_REPORT.getType(), TimelineEventTypeEnum.QUICK_REPORT.getName(), String.valueOf(quickReportDO.getId()));
    }

    @Override
    public PageResult<QuickReportVO> quickReportPage(QuickReportPageReqVO pageReqVO){
        IPage<QuickReportVO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        quickReportMapper.selectPageList(page, pageReqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public PageResult<QuickReportVO> myQuickReportPage(QuickReportPageReqVO pageReqVO){
        Long userId = WebFrameworkUtils.getLoginUserId();
        pageReqVO.setUserId(userId);
        pageReqVO.setHandler(null);
        pageReqVO.setReporter(null);
        IPage<QuickReportVO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        quickReportMapper.selectPageList(page, pageReqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public void updateQuickReport(QuickReportHandleReqVO handleReqVO){
        QuickReportDO quickReportDO = new QuickReportDO();
        quickReportDO.setId(handleReqVO.getId());
        quickReportDO.setReportTitle(handleReqVO.getReportTitle());
        quickReportDO.setReportContent(handleReqVO.getReportContent());
        quickReportDO.setStatus(handleReqVO.getStatus());
        quickReportDO.setHandleNotes(handleReqVO.getHandleNotes());
        quickReportDO.setHandleTime(handleReqVO.getHandleTime());
        quickReportDO.setTags(handleReqVO.getTags());
        quickReportDO.setAttachments(handleReqVO.getAttachments());
        quickReportMapper.updateById(quickReportDO);
    }


}
