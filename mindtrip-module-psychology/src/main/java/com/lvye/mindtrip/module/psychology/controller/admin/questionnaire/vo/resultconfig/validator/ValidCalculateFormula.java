package com.lvye.mindtrip.module.psychology.controller.admin.questionnaire.vo.resultconfig.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 计算公式验证注解
 *
 * @author MinGoo
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = CalculateFormulaValidator.class)
public @interface ValidCalculateFormula {
    
    String message() default "计算公式格式不正确，支持格式：SUM(score)、AVG(score)、MAX(score)、MIN(score)、COUNT(score)";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}

