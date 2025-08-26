package cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 计算公式验证器
 *
 * @author MinGoo
 */
public class CalculateFormulaValidator implements ConstraintValidator<ValidCalculateFormula, String> {

    @Override
    public void initialize(ValidCalculateFormula constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 如果为空，则认为是有效的（可选字段）
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // 检查是否包含有效的计算公式
        String trimmedValue = value.trim();
        
        // 支持的计算函数
        String[] validFunctions = {"SUM", "AVG", "MAX", "MIN", "COUNT"};
        
        // 检查是否以有效函数开头
        boolean hasValidFunction = false;
        for (String function : validFunctions) {
            if (trimmedValue.toUpperCase().startsWith(function + "(")) {
                hasValidFunction = true;
                break;
            }
        }
        
        if (!hasValidFunction) {
            return false;
        }
        
        // 检查括号是否匹配
        int openBrackets = 0;
        int closeBrackets = 0;
        for (char c : trimmedValue.toCharArray()) {
            if (c == '(') {
                openBrackets++;
            } else if (c == ')') {
                closeBrackets++;
            }
        }
        
        return openBrackets == closeBrackets;
    }
}

