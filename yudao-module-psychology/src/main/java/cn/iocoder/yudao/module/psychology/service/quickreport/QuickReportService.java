package cn.iocoder.yudao.module.psychology.service.quickreport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.quickreport.vo.QuickReportPageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.quickreport.vo.QuickReportSaveReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.quickreport.vo.QuickReportHandleReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.quickreport.vo.QuickReportVO;

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

}
