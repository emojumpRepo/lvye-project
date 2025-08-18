package cn.iocoder.yudao.module.psychology.service.validation;

import java.util.List;
import java.util.Map;

/**
 * 数据验证服务接口
 *
 * @author 芋道源码
 */
public interface DataValidationService {

    /**
     * 数据验证结果
     */
    class ValidationResult {
        private boolean valid;
        private String message;
        private int totalRecords;
        private int validRecords;
        private int invalidRecords;
        private List<ValidationError> errors;
        private Map<String, Object> statistics;
        private long duration;

        // 构造函数
        public ValidationResult() {}

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        
        public int getValidRecords() { return validRecords; }
        public void setValidRecords(int validRecords) { this.validRecords = validRecords; }
        
        public int getInvalidRecords() { return invalidRecords; }
        public void setInvalidRecords(int invalidRecords) { this.invalidRecords = invalidRecords; }
        
        public List<ValidationError> getErrors() { return errors; }
        public void setErrors(List<ValidationError> errors) { this.errors = errors; }
        
        public Map<String, Object> getStatistics() { return statistics; }
        public void setStatistics(Map<String, Object> statistics) { this.statistics = statistics; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
    }

    /**
     * 验证错误信息
     */
    class ValidationError {
        private String recordId;
        private String fieldName;
        private Object fieldValue;
        private String errorType;
        private String errorMessage;
        private String severity; // ERROR, WARNING, INFO

        // 构造函数
        public ValidationError() {}

        public ValidationError(String recordId, String fieldName, String errorType, String errorMessage) {
            this.recordId = recordId;
            this.fieldName = fieldName;
            this.errorType = errorType;
            this.errorMessage = errorMessage;
            this.severity = "ERROR";
        }

        // Getters and Setters
        public String getRecordId() { return recordId; }
        public void setRecordId(String recordId) { this.recordId = recordId; }
        
        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }
        
        public Object getFieldValue() { return fieldValue; }
        public void setFieldValue(Object fieldValue) { this.fieldValue = fieldValue; }
        
        public String getErrorType() { return errorType; }
        public void setErrorType(String errorType) { this.errorType = errorType; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }

    /**
     * 数据质量报告
     */
    class DataQualityReport {
        private String dataType;
        private int totalRecords;
        private double overallQualityScore; // 0-100
        private Map<String, Double> fieldQualityScores;
        private Map<String, Integer> errorTypeCounts;
        private List<String> recommendations;
        private long generateTime;

        // 构造函数
        public DataQualityReport() {}

        public DataQualityReport(String dataType) {
            this.dataType = dataType;
            this.generateTime = System.currentTimeMillis();
        }

        // Getters and Setters
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        
        public double getOverallQualityScore() { return overallQualityScore; }
        public void setOverallQualityScore(double overallQualityScore) { this.overallQualityScore = overallQualityScore; }
        
        public Map<String, Double> getFieldQualityScores() { return fieldQualityScores; }
        public void setFieldQualityScores(Map<String, Double> fieldQualityScores) { this.fieldQualityScores = fieldQualityScores; }
        
        public Map<String, Integer> getErrorTypeCounts() { return errorTypeCounts; }
        public void setErrorTypeCounts(Map<String, Integer> errorTypeCounts) { this.errorTypeCounts = errorTypeCounts; }
        
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
        
        public long getGenerateTime() { return generateTime; }
        public void setGenerateTime(long generateTime) { this.generateTime = generateTime; }
    }

    /**
     * 数据完整性检查
     *
     * @param dataType 数据类型 (questionnaire, questionnaire_result)
     * @return 验证结果
     */
    ValidationResult checkDataIntegrity(String dataType);

    /**
     * 数据一致性验证
     *
     * @param dataType 数据类型
     * @return 验证结果
     */
    ValidationResult validateDataConsistency(String dataType);

    /**
     * 数据质量评估
     *
     * @param dataType 数据类型
     * @return 数据质量报告
     */
    DataQualityReport assessDataQuality(String dataType);

    /**
     * 字段级别验证
     *
     * @param dataType 数据类型
     * @param fieldName 字段名称
     * @return 验证结果
     */
    ValidationResult validateField(String dataType, String fieldName);

    /**
     * 业务规则验证
     *
     * @param dataType 数据类型
     * @param ruleNames 规则名称列表，为空则验证所有规则
     * @return 验证结果
     */
    ValidationResult validateBusinessRules(String dataType, List<String> ruleNames);

    /**
     * 数据关联性验证
     *
     * @param primaryDataType 主数据类型
     * @param relatedDataType 关联数据类型
     * @return 验证结果
     */
    ValidationResult validateDataRelationships(String primaryDataType, String relatedDataType);

    /**
     * 重复数据检测
     *
     * @param dataType 数据类型
     * @param checkFields 检查字段列表
     * @return 验证结果
     */
    ValidationResult detectDuplicateData(String dataType, List<String> checkFields);

    /**
     * 数据范围验证
     *
     * @param dataType 数据类型
     * @param fieldName 字段名称
     * @param minValue 最小值
     * @param maxValue 最大值
     * @return 验证结果
     */
    ValidationResult validateDataRange(String dataType, String fieldName, Object minValue, Object maxValue);

    /**
     * 数据格式验证
     *
     * @param dataType 数据类型
     * @param fieldName 字段名称
     * @param pattern 正则表达式模式
     * @return 验证结果
     */
    ValidationResult validateDataFormat(String dataType, String fieldName, String pattern);

    /**
     * 生成验证报告
     *
     * @param validationResults 验证结果列表
     * @return 报告内容
     */
    String generateValidationReport(List<ValidationResult> validationResults);

    /**
     * 获取数据统计信息
     *
     * @param dataType 数据类型
     * @return 统计信息
     */
    Map<String, Object> getDataStatistics(String dataType);

    /**
     * 修复数据问题
     *
     * @param dataType 数据类型
     * @param fixRules 修复规则
     * @param dryRun 是否为试运行
     * @return 修复结果
     */
    ValidationResult fixDataIssues(String dataType, Map<String, Object> fixRules, boolean dryRun);

}