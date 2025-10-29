package cn.iocoder.yudao.module.psychology.controller.admin.workspace;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.psychology.controller.admin.workspace.vo.WorkspaceDataPageReqVO;
import cn.iocoder.yudao.module.psychology.service.workspace.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 工作台 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 工作台")
@RestController
@RequestMapping("/psychology/workspace")
@Validated
@Slf4j
public class WorkspaceController {

    @Resource
    private WorkspaceService workspaceService;

    @GetMapping("/data")
    @Operation(summary = "获取工作台数据分页",
               description = "根据数据类型（TODAY_CONSULTATIONS/HIGH_RISK_STUDENTS/PENDING_ALERTS）返回对应的分页数据")
    @DataPermission(enable = false)
    public CommonResult<PageResult<?>> getWorkspaceData(@Valid WorkspaceDataPageReqVO pageReqVO) {
        PageResult<?> pageResult = workspaceService.getWorkspaceDataPage(pageReqVO);
        return success(pageResult);
    }
}
