package com.lvye.mindtrip.module.crm.framework.web.config;

import com.lvye.mindtrip.framework.swagger.config.MindtripSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * crm 模块的 web 组件的 Configuration
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
public class CrmWebConfiguration {

    /**
     * crm 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi crmGroupedOpenApi() {
        return MindtripSwaggerAutoConfiguration.buildGroupedOpenApi("crm");
    }

}
