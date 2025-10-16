package cn.iocoder.yudao.module.psychology.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.psychology.service.assessment.AssessmentTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-29
 * @Description:测评任务状态更新定时任务
 * @Version: 1.0
 */
@Component
@Slf4j
public class AssessmentTaskStateJob implements JobHandler {

    @Autowired
    private AssessmentTaskService assessmentTaskService;

    @Override
    @TenantJob
    public String execute(String param){
        log.info("开始更新过期测评任务状态");
        assessmentTaskService.updateExpireStatus();
        log.info("更新过期测评任务状态完成");
        return "SUCCESS";
    }
}
