package com.lvye.mindtrip.module.iot.framework.web.config;

import com.lvye.mindtrip.framework.swagger.config.MindtripSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * iot 模块的 web 组件的 Configuration
 *
 * @author ahh
 */
@Configuration(proxyBeanMethods = false)
public class IotWebConfiguration {

    /**
     * iot 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi iotGroupedOpenApi() {
        return MindtripSwaggerAutoConfiguration.buildGroupedOpenApi("iot");
    }

}
