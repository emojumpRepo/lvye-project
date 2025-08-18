package cn.iocoder.yudao.module.psychology.service.migration;

import java.util.List;
import java.util.Map;

/**
 * 数据迁移服务接口
 *
 * @author 芋道源码
 */
public interface DataMigrationService {

    /**
     * 数据迁移结果
     */
    class MigrationResult {
        private boolean success;
        private String message;
        private int totalRecords;
        private int successRecords;
        private int failedRecords;
        private List<String> errors;
        private long duration;

        // 构造函数
        public MigrationResult() {}

        public MigrationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        
        public int getSuccessRecords() { return successRecords; }
        public void setSuccessRecords(int successRecords) { this.successRecords = successRecords; }
        
        public int getFailedRecords() { return failedRecords; }
        public void setFailedRecords(int failedRecords) { this.failedRecords = failedRecords; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
    }

    /**
     * 迁移进度信息
     */
    class MigrationProgress {
        private String taskId;
        private String taskName;
        private int totalRecords;
        private int processedRecords;
        private int successRecords;
        private int failedRecords;
        private double progressPercentage;
        private String currentStep;
        private boolean completed;
        private String status; // RUNNING, COMPLETED, FAILED, CANCELLED
        private long startTime;
        private long endTime;
        private List<String> recentErrors;

        // 构造函数
        public MigrationProgress() {}

        public MigrationProgress(String taskId, String taskName) {
            this.taskId = taskId;
            this.taskName = taskName;
            this.startTime = System.currentTimeMillis();
            this.status = "RUNNING";
        }

        // Getters and Setters
        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        
        public int getProcessedRecords() { return processedRecords; }
        public void setProcessedRecords(int processedRecords) { this.processedRecords = processedRecords; }
        
        public int getSuccessRecords() { return successRecords; }
        public void setSuccessRecords(int successRecords) { this.successRecords = successRecords; }
        
        public int getFailedRecords() { return failedRecords; }
        public void setFailedRecords(int failedRecords) { this.failedRecords = failedRecords; }
        
        public double getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
        
        public String getCurrentStep() { return currentStep; }
        public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
        
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        
        public List<String> getRecentErrors() { return recentErrors; }
        public void setRecentErrors(List<String> recentErrors) { this.recentErrors = recentErrors; }
    }

    /**
     * 从emojump系统提取问卷数据
     *
     * @param sourceConfig 源系统配置
     * @return 迁移结果
     */
    MigrationResult extractQuestionnaireData(Map<String, Object> sourceConfig);

    /**
     * 从emojump系统提取问卷结果数据
     *
     * @param sourceConfig 源系统配置
     * @param questionnaireIds 要提取的问卷ID列表，为空则提取所有
     * @return 迁移结果
     */
    MigrationResult extractQuestionnaireResultData(Map<String, Object> sourceConfig, List<Long> questionnaireIds);

    /**
     * 数据格式转换和清洗
     *
     * @param rawData 原始数据
     * @param dataType 数据类型 (questionnaire, questionnaire_result)
     * @return 迁移结果
     */
    MigrationResult transformAndCleanData(List<Map<String, Object>> rawData, String dataType);

    /**
     * 数据导入和验证
     *
     * @param cleanedData 清洗后的数据
     * @param dataType 数据类型
     * @param validateOnly 是否只验证不导入
     * @return 迁移结果
     */
    MigrationResult importAndValidateData(List<Map<String, Object>> cleanedData, String dataType, boolean validateOnly);

    /**
     * 启动异步迁移任务
     *
     * @param taskName 任务名称
     * @param sourceConfig 源系统配置
     * @param migrationConfig 迁移配置
     * @return 任务ID
     */
    String startAsyncMigration(String taskName, Map<String, Object> sourceConfig, Map<String, Object> migrationConfig);

    /**
     * 获取迁移进度
     *
     * @param taskId 任务ID
     * @return 迁移进度
     */
    MigrationProgress getMigrationProgress(String taskId);

    /**
     * 取消迁移任务
     *
     * @param taskId 任务ID
     * @return 是否成功取消
     */
    boolean cancelMigration(String taskId);

    /**
     * 获取所有迁移任务列表
     *
     * @return 任务列表
     */
    List<MigrationProgress> getAllMigrationTasks();

    /**
     * 清理完成的迁移任务
     *
     * @param olderThanDays 清理多少天前的任务
     * @return 清理的任务数量
     */
    int cleanupCompletedTasks(int olderThanDays);

    /**
     * 生成迁移报告
     *
     * @param taskId 任务ID
     * @return 报告内容
     */
    String generateMigrationReport(String taskId);

    /**
     * 验证数据完整性
     *
     * @param dataType 数据类型
     * @return 验证结果
     */
    MigrationResult validateDataIntegrity(String dataType);

}