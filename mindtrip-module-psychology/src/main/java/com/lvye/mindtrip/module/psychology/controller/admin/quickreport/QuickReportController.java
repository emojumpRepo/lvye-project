package com.lvye.mindtrip.module.psychology.controller.admin.quickreport;

import com.lvye.mindtrip.framework.common.pojo.CommonResult;
import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.framework.common.util.object.BeanUtils;
import com.lvye.mindtrip.framework.datapermission.core.annotation.DataPermission;
import com.lvye.mindtrip.module.psychology.controller.admin.quickreport.vo.QuickReportPageReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.quickreport.vo.QuickReportSaveReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.quickreport.vo.QuickReportHandleReqVO;
import com.lvye.mindtrip.module.psychology.controller.admin.quickreport.vo.QuickReportVO;
import com.lvye.mindtrip.module.psychology.service.quickreport.QuickReportService;
import com.lvye.mindtrip.module.system.api.user.dto.QuickReportHandleUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.lvye.mindtrip.framework.common.pojo.CommonResult.success;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-31
 * @Description:快速上报
 * @Version: 1.0
 */
@Tag(name = "管理后台 - 快速上报")
@RestController
@RequestMapping("/psychology/quick-report")
@Validated
@Slf4j
public class QuickReportController {

    @Autowired
    private QuickReportService quickReportService;

    @PostMapping("/create")
    @Operation(summary = "创建快速上报")
    @DataPermission(enable = false)
    public CommonResult<Boolean> createQuickReport(@Valid @RequestBody QuickReportSaveReqVO saveReqVO) {
        quickReportService.saveQuickReport(saveReqVO);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得快速上报分页")
    @DataPermission(enable = false)
    public CommonResult<PageResult<QuickReportVO>> getQuickReportPage(@Valid QuickReportPageReqVO pageReqVO) {
        PageResult<QuickReportVO> pageResult = quickReportService.quickReportPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, QuickReportVO.class));
    }

    @GetMapping("/my-page")
    @Operation(summary = "获得我的快速上报分页")
    @DataPermission(enable = false)
    public CommonResult<PageResult<QuickReportVO>> getMyQuickReportPage(@Valid QuickReportPageReqVO pageReqVO) {
        PageResult<QuickReportVO> pageResult = quickReportService.myQuickReportPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, QuickReportVO.class));
    }

    @PostMapping("/handle")
    @Operation(summary = "处理快速上报信息")
    @DataPermission(enable = false)
    public CommonResult<Boolean> updateQuickReport(@Valid @RequestBody QuickReportHandleReqVO updateReqVO) {
        quickReportService.updateQuickReport(updateReqVO);
        return success(true);
    }

    @GetMapping("/teacher-list")
    @Operation(summary = "获得选择负责人列表")
    @DataPermission(enable = false)
    public CommonResult<List<QuickReportHandleUserVO>> selectHandleUserList(@RequestParam Long studentProfileId) {
        List<QuickReportHandleUserVO> userList = quickReportService.selectHandleUserList(studentProfileId);
        return success(userList);
    }

}
