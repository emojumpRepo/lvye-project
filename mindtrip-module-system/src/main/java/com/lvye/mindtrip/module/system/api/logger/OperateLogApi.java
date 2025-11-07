package com.lvye.mindtrip.module.system.api.logger;

import com.lvye.mindtrip.framework.common.biz.system.logger.OperateLogCommonApi;
import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.module.system.api.logger.dto.OperateLogPageReqDTO;
import com.lvye.mindtrip.module.system.api.logger.dto.OperateLogRespDTO;

/**
 * 操作日志 API 接口
 *
 * @author 芋道源码
 */
public interface OperateLogApi extends OperateLogCommonApi {

    /**
     * 获取指定模块的指定数据的操作日志分页
     *
     * @param pageReqDTO 请求
     * @return 操作日志分页
     */
    PageResult<OperateLogRespDTO> getOperateLogPage(OperateLogPageReqDTO pageReqDTO);

}
