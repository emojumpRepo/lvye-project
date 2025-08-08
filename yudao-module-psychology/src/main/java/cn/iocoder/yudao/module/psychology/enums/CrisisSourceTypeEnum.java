package cn.iocoder.yudao.module.psychology.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CrisisSourceTypeEnum {
    QUICK_REPORT(1, "快速上报"),
    AUTO_RISK(2, "自动预警"),
    MANUAL_CREATE(3, "手动创建"),
    REFERRAL(4, "转介");

    private final Integer type;
    private final String name;
}


