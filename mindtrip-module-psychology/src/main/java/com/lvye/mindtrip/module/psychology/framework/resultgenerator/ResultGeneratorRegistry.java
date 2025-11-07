package com.lvye.mindtrip.module.psychology.framework.resultgenerator;

import com.lvye.mindtrip.module.psychology.enums.ResultGeneratorTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 结果生成器注册器
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class ResultGeneratorRegistry {

    private final List<ResultGeneratorStrategy> generators;
    private final Map<ResultGeneratorTypeEnum, ResultGeneratorStrategy> generatorMap = new ConcurrentHashMap<>();

    public ResultGeneratorRegistry(List<ResultGeneratorStrategy> generators) {
        this.generators = generators;
    }

    @PostConstruct
    public void init() {
        registerGenerators();
    }

    /**
     * 注册所有生成器
     */
    private void registerGenerators() {
        for (ResultGeneratorStrategy generator : generators) {
            registerGenerator(generator);
        }
        log.info("Registered {} result generators", generatorMap.size());
    }

    /**
     * 注册单个生成器
     *
     * @param generator 生成器
     */
    public void registerGenerator(ResultGeneratorStrategy generator) {
        ResultGeneratorTypeEnum type = generator.getGeneratorType();
        generatorMap.put(type, generator);
        log.debug("Registered result generator: {} -> {}", type, generator.getClass().getSimpleName());
    }

    /**
     * 获取生成器
     *
     * @param type 生成器类型
     * @return 生成器
     */
    public ResultGeneratorStrategy getGenerator(ResultGeneratorTypeEnum type) {
        return generatorMap.get(type);
    }

    /**
     * 检查生成器是否已注册
     *
     * @param type 生成器类型
     * @return 是否已注册
     */
    public boolean isRegistered(ResultGeneratorTypeEnum type) {
        return generatorMap.containsKey(type);
    }

}