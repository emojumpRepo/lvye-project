package cn.iocoder.yudao.module.psychology.service.workspace;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.workspace.vo.WorkspaceDataPageReqVO;

/**
 * 工作台 Service 接口
 *
 * @author 芋道源码
 */
public interface WorkspaceService {

    /**
     * 获取工作台数据分页
     * 根据不同的数据类型，返回对应的分页数据
     *
     * @param pageReqVO 分页查询参数
     * @return 分页数据（返回类型根据 type 不同而不同）
     */
    PageResult<?> getWorkspaceDataPage(WorkspaceDataPageReqVO pageReqVO);
}
