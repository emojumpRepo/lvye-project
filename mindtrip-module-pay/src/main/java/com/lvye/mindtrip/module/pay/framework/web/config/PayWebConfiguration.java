package com.lvye.mindtrip.module.pay.framework.web.config;

import com.lvye.mindtrip.framework.swagger.config.MindtripSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * pay 模块的 web 组件的 Configuration
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
public class PayWebConfiguration {

    /**
     * pay 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi payGroupedOpenApi() {
        return MindtripSwaggerAutoConfiguration.buildGroupedOpenApi("pay");
    }

}
