package com.lvye.mindtrip.module.iot.service.rule.scene.action;

import cn.hutool.core.collection.CollUtil;
import com.lvye.mindtrip.framework.common.enums.CommonStatusEnum;
import com.lvye.mindtrip.module.iot.core.mq.message.IotDeviceMessage;
import com.lvye.mindtrip.module.iot.dal.dataobject.alert.IotAlertConfigDO;
import com.lvye.mindtrip.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import com.lvye.mindtrip.module.iot.enums.rule.IotSceneRuleActionTypeEnum;
import com.lvye.mindtrip.module.iot.service.alert.IotAlertConfigService;
import com.lvye.mindtrip.module.iot.service.alert.IotAlertRecordService;
import com.lvye.mindtrip.module.system.api.mail.MailSendApi;
import com.lvye.mindtrip.module.system.api.notify.NotifyMessageSendApi;
import com.lvye.mindtrip.module.system.api.sms.SmsSendApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

// TODO @puhui999、@芋艿：未测试；需要场景联动开发完
/**
 * IoT 告警触发的 {@link IotSceneRuleAction} 实现类
 *
 * @author 芋道源码
 */
@Component
public class IotAlertTriggerSceneRuleAction implements IotSceneRuleAction {

    @Resource
    private IotAlertConfigService alertConfigService;
    @Resource
    private IotAlertRecordService alertRecordService;

    @Resource
    private SmsSendApi smsSendApi;
    @Resource
    private MailSendApi mailSendApi;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Override
    public void execute(@Nullable IotDeviceMessage message,
                        IotSceneRuleDO rule, IotSceneRuleDO.Action actionConfig) throws Exception {
        List<IotAlertConfigDO> alertConfigs = alertConfigService.getAlertConfigListBySceneRuleIdAndStatus(
                rule.getId(), CommonStatusEnum.ENABLE.getStatus());
        if (CollUtil.isEmpty(alertConfigs)) {
            return;
        }
        alertConfigs.forEach(alertConfig -> {
            // 记录告警记录，传递场景规则ID
            alertRecordService.createAlertRecord(alertConfig, rule.getId(), message);
            // 发送告警消息
            sendAlertMessage(alertConfig, message);
        });
    }

    private void sendAlertMessage(IotAlertConfigDO config, IotDeviceMessage deviceMessage) {
        // TODO @芋艿：等场景联动开发完，再实现
        // TODO @芋艿：短信
        // TODO @芋艿：邮箱
        // TODO @芋艿：站内信
    }

    @Override
    public IotSceneRuleActionTypeEnum getType() {
        return IotSceneRuleActionTypeEnum.ALERT_TRIGGER;
    }

}
