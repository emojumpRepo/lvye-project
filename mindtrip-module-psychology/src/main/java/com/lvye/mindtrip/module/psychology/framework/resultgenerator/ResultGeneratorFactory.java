package com.lvye.mindtrip.module.psychology.framework.resultgenerator;

import com.lvye.mindtrip.module.psychology.enums.ResultGeneratorTypeEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 结果生成器工厂
 *
 * @author 芋道源码
 */
@Component
public class ResultGeneratorFactory {

    private final Map<ResultGeneratorTypeEnum, ResultGeneratorStrategy> generators;

    public ResultGeneratorFactory(List<ResultGeneratorStrategy> generatorList) {
        this.generators = generatorList.stream()
                .collect(Collectors.toMap(
                    ResultGeneratorStrategy::getGeneratorType,
                    Function.identity()
                ));
    }

    /**
     * 获取结果生成器
     *
     * @param type 生成器类型
     * @return 结果生成器
     */
    public ResultGeneratorStrategy getGenerator(ResultGeneratorTypeEnum type) {
        ResultGeneratorStrategy generator = generators.get(type);
        if (generator == null) {
            throw new IllegalArgumentException("Unsupported generator type: " + type);
        }
        return generator;
    }

    /**
     * 生成结果
     *
     * @param type 生成器类型
     * @param context 生成上下文
     * @param <T> 结果类型
     * @return 生成的结果
     */
    public <T> T generateResult(ResultGeneratorTypeEnum type, ResultGenerationContext context) {
        ResultGeneratorStrategy generator = getGenerator(type);
        generator.validateGenerationParams(context);
        return generator.generateResult(context);
    }

}