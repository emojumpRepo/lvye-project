package com.lvye.mindtrip.module.psychology.service.quickreport;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.module.psychology.controller.admin.quickreport.vo.*;
import com.lvye.mindtrip.module.system.api.user.dto.QuickReportHandleUserVO;

import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-31
 * @Description:快速上报服务层
 * @Version: 1.0
 */
public interface QuickReportService {


    /**
     * 保存快速上报信息
     * @param saveReqVO
     */
    void saveQuickReport(QuickReportSaveReqVO saveReqVO);

    /**
     * 查询快速上报列表
     * @param pageReqVO
     */
    PageResult<QuickReportVO> quickReportPage(QuickReportPageReqVO pageReqVO);

    /**
     * 查询我的快速上报列表
     * @param pageReqVO
     */
    PageResult<QuickReportVO> myQuickReportPage(QuickReportPageReqVO pageReqVO);

    /**
     * 更新快速上报信息
     * @param handleReqVO
     */
    void updateQuickReport(QuickReportHandleReqVO handleReqVO);

    /**
     * 快速上报负责人选择列表
     * @return
     */
    List<QuickReportHandleUserVO> selectHandleUserList(Long studentProfileId);

}
