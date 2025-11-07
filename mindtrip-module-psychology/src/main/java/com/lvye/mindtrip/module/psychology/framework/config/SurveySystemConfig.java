package com.lvye.mindtrip.module.psychology.framework.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.time.Duration;

/**
 * 外部问卷系统配置类
 *
 * @author 芋道源码
 */
@Configuration
public class SurveySystemConfig {

    @Resource
    private SurveySystemProperties surveySystemProperties;

}
