package cn.iocoder.yudao.module.psychology.framework.resultgenerator;

import cn.iocoder.yudao.module.psychology.enums.ResultGeneratorTypeEnum;

/**
 * 结果生成器策略接口
 *
 * @author 芋道源码
 */
public interface ResultGeneratorStrategy {

    /**
     * 获取生成器类型
     *
     * @return 生成器类型
     */
    ResultGeneratorTypeEnum getGeneratorType();

    /**
     * 检查是否支持该问卷/测评
     *
     * @param targetId 目标ID（问卷ID或测评ID）
     * @param type 生成器类型
     * @return 是否支持
     */
    boolean supports(Long targetId, ResultGeneratorTypeEnum type);

    /**
     * 生成结果
     *
     * @param context 生成上下文
     * @param <T> 结果类型
     * @return 生成的结果
     */
    <T> T generateResult(ResultGenerationContext context);

    /**
     * 验证生成参数
     *
     * @param context 生成上下文
     * @throws IllegalArgumentException 参数无效时抛出异常
     */
    void validateGenerationParams(ResultGenerationContext context);

}