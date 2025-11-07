package com.lvye.mindtrip.module.crm.job.customer;

import com.lvye.mindtrip.framework.quartz.core.handler.JobHandler;
import com.lvye.mindtrip.framework.tenant.core.job.TenantJob;
import com.lvye.mindtrip.module.crm.service.customer.CrmCustomerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 客户自动掉入公海 Job
 *
 * @author 芋道源码
 */
@Component
public class CrmCustomerAutoPutPoolJob implements JobHandler {

    @Resource
    private CrmCustomerService customerService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = customerService.autoPutCustomerPool();
        return String.format("掉入公海客户 %s 个", count);
    }

}