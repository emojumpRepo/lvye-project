package com.lvye.mindtrip.module.system.framework.datapermission.config;

import com.lvye.mindtrip.module.system.dal.dataobject.dept.DeptDO;
import com.lvye.mindtrip.module.system.dal.dataobject.user.AdminUserDO;
import com.lvye.mindtrip.module.system.dal.dataobject.permission.UserDeptDO;
import com.lvye.mindtrip.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system 模块的数据权限 Configuration
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
public class DataPermissionConfiguration {

    @Bean
    public DeptDataPermissionRuleCustomizer sysDeptDataPermissionRuleCustomizer() {
        return rule -> {
            // dept
            // rule.addDeptColumn(AdminUserDO.class);
            rule.addDeptColumn(DeptDO.class, "id");
            rule.addDeptColumn(UserDeptDO.class, "deptId");
            // user
            rule.addUserColumn(AdminUserDO.class, "id");
        };
    }

}
