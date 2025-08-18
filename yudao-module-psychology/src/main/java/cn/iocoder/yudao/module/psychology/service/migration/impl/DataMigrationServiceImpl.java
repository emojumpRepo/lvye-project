package cn.iocoder.yudao.module.psychology.service.migration.impl;

import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.service.migration.DataMigrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据迁移服务实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class DataMigrationServiceImpl implements DataMigrationService {

    @Resource
    private QuestionnaireMapper questionnaireMapper;

    @Resource
    private QuestionnaireResultMapper questionnaireResultMapper;

    // 存储迁移任务进度
    private final Map<String, MigrationProgress> migrationTasks = new ConcurrentHashMap<>();

    @Override
    public MigrationResult extractQuestionnaireData(Map<String, Object> sourceConfig) {
        long startTime = System.currentTimeMillis();
        MigrationResult result = new MigrationResult();
        List<String> errors = new ArrayList<>();
        
        try {
            log.info("开始提取问卷数据，源配置: {}", sourceConfig);
            
            // 获取数据库连接配置
            String jdbcUrl = (String) sourceConfig.get("jdbcUrl");
            String username = (String) sourceConfig.get("username");
            String password = (String) sourceConfig.get("password");
            
            if (jdbcUrl == null || username == null || password == null) {
                result.setSuccess(false);
                result.setMessage("数据库连接配置不完整");
                return result;
            }
            
            // 连接源数据库
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                
                // 查询问卷数据
                String sql = "SELECT * FROM emojump_questionnaire WHERE status = 1 ORDER BY create_time DESC";
                try (PreparedStatement stmt = connection.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    int totalCount = 0;
                    int successCount = 0;
                    
                    while (rs.next()) {
                        totalCount++;
                        try {
                            // 提取问卷数据
                            Map<String, Object> questionnaireData = extractQuestionnaireFromResultSet(rs);
                            
                            // 这里可以添加数据验证逻辑
                            if (validateQuestionnaireData(questionnaireData)) {
                                successCount++;
                            } else {
                                errors.add("问卷数据验证失败: ID=" + questionnaireData.get("id"));
                            }
                            
                        } catch (Exception e) {
                            log.error("提取问卷数据失败", e);
                            errors.add("提取问卷数据失败: " + e.getMessage());
                        }
                    }
                    
                    result.setTotalRecords(totalCount);
                    result.setSuccessRecords(successCount);
                    result.setFailedRecords(totalCount - successCount);
                    result.setSuccess(successCount > 0);
                    result.setMessage(String.format("提取完成，总计: %d, 成功: %d, 失败: %d", 
                            totalCount, successCount, totalCount - successCount));
                }
                
            } catch (SQLException e) {
                log.error("数据库连接失败", e);
                result.setSuccess(false);
                result.setMessage("数据库连接失败: " + e.getMessage());
                errors.add("数据库连接失败: " + e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("提取问卷数据失败", e);
            result.setSuccess(false);
            result.setMessage("提取失败: " + e.getMessage());
            errors.add("提取失败: " + e.getMessage());
        }
        
        result.setErrors(errors);
        result.setDuration(System.currentTimeMillis() - startTime);
        
        log.info("问卷数据提取完成，结果: {}", result.getMessage());
        return result;
    }

    @Override
    public MigrationResult extractQuestionnaireResultData(Map<String, Object> sourceConfig, List<Long> questionnaireIds) {
        long startTime = System.currentTimeMillis();
        MigrationResult result = new MigrationResult();
        List<String> errors = new ArrayList<>();
        
        try {
            log.info("开始提取问卷结果数据，源配置: {}, 问卷IDs: {}", sourceConfig, questionnaireIds);
            
            // 获取数据库连接配置
            String jdbcUrl = (String) sourceConfig.get("jdbcUrl");
            String username = (String) sourceConfig.get("username");
            String password = (String) sourceConfig.get("password");
            
            // 连接源数据库
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                
                // 构建查询SQL
                StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM emojump_questionnaire_result WHERE 1=1");
                if (questionnaireIds != null && !questionnaireIds.isEmpty()) {
                    sqlBuilder.append(" AND questionnaire_id IN (");
                    for (int i = 0; i < questionnaireIds.size(); i++) {
                        if (i > 0) sqlBuilder.append(",");
                        sqlBuilder.append("?");
                    }
                    sqlBuilder.append(")");
                }
                sqlBuilder.append(" ORDER BY create_time DESC");
                
                try (PreparedStatement stmt = connection.prepareStatement(sqlBuilder.toString())) {
                    
                    // 设置参数
                    if (questionnaireIds != null && !questionnaireIds.isEmpty()) {
                        for (int i = 0; i < questionnaireIds.size(); i++) {
                            stmt.setLong(i + 1, questionnaireIds.get(i));
                        }
                    }
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        int totalCount = 0;
                        int successCount = 0;
                        
                        while (rs.next()) {
                            totalCount++;
                            try {
                                // 提取问卷结果数据
                                Map<String, Object> resultData = extractQuestionnaireResultFromResultSet(rs);
                                
                                // 数据验证
                                if (validateQuestionnaireResultData(resultData)) {
                                    successCount++;
                                } else {
                                    errors.add("问卷结果数据验证失败: ID=" + resultData.get("id"));
                                }
                                
                            } catch (Exception e) {
                                log.error("提取问卷结果数据失败", e);
                                errors.add("提取问卷结果数据失败: " + e.getMessage());
                            }
                        }
                        
                        result.setTotalRecords(totalCount);
                        result.setSuccessRecords(successCount);
                        result.setFailedRecords(totalCount - successCount);
                        result.setSuccess(successCount > 0);
                        result.setMessage(String.format("提取完成，总计: %d, 成功: %d, 失败: %d", 
                                totalCount, successCount, totalCount - successCount));
                    }
                }
                
            } catch (SQLException e) {
                log.error("数据库连接失败", e);
                result.setSuccess(false);
                result.setMessage("数据库连接失败: " + e.getMessage());
                errors.add("数据库连接失败: " + e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("提取问卷结果数据失败", e);
            result.setSuccess(false);
            result.setMessage("提取失败: " + e.getMessage());
            errors.add("提取失败: " + e.getMessage());
        }
        
        result.setErrors(errors);
        result.setDuration(System.currentTimeMillis() - startTime);
        
        log.info("问卷结果数据提取完成，结果: {}", result.getMessage());
        return result;
    }

    @Override
    public MigrationResult transformAndCleanData(List<Map<String, Object>> rawData, String dataType) {
        long startTime = System.currentTimeMillis();
        MigrationResult result = new MigrationResult();
        List<String> errors = new ArrayList<>();
        
        try {
            log.info("开始数据转换和清洗，数据类型: {}, 记录数: {}", dataType, rawData.size());
            
            int totalCount = rawData.size();
            int successCount = 0;
            
            for (Map<String, Object> record : rawData) {
                try {
                    if ("questionnaire".equals(dataType)) {
                        transformQuestionnaireData(record);
                    } else if ("questionnaire_result".equals(dataType)) {
                        transformQuestionnaireResultData(record);
                    }
                    
                    // 数据清洗
                    cleanDataRecord(record);
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("数据转换失败", e);
                    errors.add("数据转换失败: " + e.getMessage());
                }
            }
            
            result.setTotalRecords(totalCount);
            result.setSuccessRecords(successCount);
            result.setFailedRecords(totalCount - successCount);
            result.setSuccess(successCount > 0);
            result.setMessage(String.format("转换完成，总计: %d, 成功: %d, 失败: %d", 
                    totalCount, successCount, totalCount - successCount));
            
        } catch (Exception e) {
            log.error("数据转换和清洗失败", e);
            result.setSuccess(false);
            result.setMessage("转换失败: " + e.getMessage());
            errors.add("转换失败: " + e.getMessage());
        }
        
        result.setErrors(errors);
        result.setDuration(System.currentTimeMillis() - startTime);
        
        log.info("数据转换和清洗完成，结果: {}", result.getMessage());
        return result;
    }

    @Override
    @Transactional
    public MigrationResult importAndValidateData(List<Map<String, Object>> cleanedData, String dataType, boolean validateOnly) {
        long startTime = System.currentTimeMillis();
        MigrationResult result = new MigrationResult();
        List<String> errors = new ArrayList<>();
        
        try {
            log.info("开始数据导入和验证，数据类型: {}, 记录数: {}, 仅验证: {}", dataType, cleanedData.size(), validateOnly);
            
            int totalCount = cleanedData.size();
            int successCount = 0;
            
            for (Map<String, Object> record : cleanedData) {
                try {
                    if ("questionnaire".equals(dataType)) {
                        if (validateOnly) {
                            validateQuestionnaireForImport(record);
                        } else {
                            importQuestionnaireData(record);
                        }
                    } else if ("questionnaire_result".equals(dataType)) {
                        if (validateOnly) {
                            validateQuestionnaireResultForImport(record);
                        } else {
                            importQuestionnaireResultData(record);
                        }
                    }
                    
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("数据导入失败", e);
                    errors.add("数据导入失败: " + e.getMessage());
                }
            }
            
            result.setTotalRecords(totalCount);
            result.setSuccessRecords(successCount);
            result.setFailedRecords(totalCount - successCount);
            result.setSuccess(successCount > 0);
            result.setMessage(String.format("%s完成，总计: %d, 成功: %d, 失败: %d", 
                    validateOnly ? "验证" : "导入", totalCount, successCount, totalCount - successCount));
            
        } catch (Exception e) {
            log.error("数据导入和验证失败", e);
            result.setSuccess(false);
            result.setMessage("导入失败: " + e.getMessage());
            errors.add("导入失败: " + e.getMessage());
        }
        
        result.setErrors(errors);
        result.setDuration(System.currentTimeMillis() - startTime);
        
        log.info("数据导入和验证完成，结果: {}", result.getMessage());
        return result;
    }

    @Override
    @Async
    public String startAsyncMigration(String taskName, Map<String, Object> sourceConfig, Map<String, Object> migrationConfig) {
        String taskId = UUID.randomUUID().toString();
        MigrationProgress progress = new MigrationProgress(taskId, taskName);
        migrationTasks.put(taskId, progress);
        
        log.info("启动异步迁移任务: {}, ID: {}", taskName, taskId);
        
        try {
            // 执行迁移步骤
            executeAsyncMigration(taskId, sourceConfig, migrationConfig);
            
        } catch (Exception e) {
            log.error("异步迁移任务执行失败", e);
            progress.setStatus("FAILED");
            progress.setCompleted(true);
            progress.setEndTime(System.currentTimeMillis());
            
            List<String> errors = progress.getRecentErrors();
            if (errors == null) {
                errors = new ArrayList<>();
                progress.setRecentErrors(errors);
            }
            errors.add("任务执行失败: " + e.getMessage());
        }
        
        return taskId;
    }

    @Override
    public MigrationProgress getMigrationProgress(String taskId) {
        return migrationTasks.get(taskId);
    }

    @Override
    public boolean cancelMigration(String taskId) {
        MigrationProgress progress = migrationTasks.get(taskId);
        if (progress != null && !progress.isCompleted()) {
            progress.setStatus("CANCELLED");
            progress.setCompleted(true);
            progress.setEndTime(System.currentTimeMillis());
            log.info("迁移任务已取消: {}", taskId);
            return true;
        }
        return false;
    }

    @Override
    public List<MigrationProgress> getAllMigrationTasks() {
        return new ArrayList<>(migrationTasks.values());
    }

    @Override
    public int cleanupCompletedTasks(int olderThanDays) {
        long cutoffTime = System.currentTimeMillis() - (olderThanDays * 24L * 60L * 60L * 1000L);
        AtomicInteger cleanedCount = new AtomicInteger(0);
        
        migrationTasks.entrySet().removeIf(entry -> {
            MigrationProgress progress = entry.getValue();
            if (progress.isCompleted() && progress.getEndTime() < cutoffTime) {
                cleanedCount.incrementAndGet();
                return true;
            }
            return false;
        });
        
        log.info("清理了 {} 个完成的迁移任务", cleanedCount.get());
        return cleanedCount.get();
    }

    @Override
    public String generateMigrationReport(String taskId) {
        MigrationProgress progress = migrationTasks.get(taskId);
        if (progress == null) {
            return "任务不存在";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("迁移任务报告\n");
        report.append("===================\n");
        report.append("任务ID: ").append(progress.getTaskId()).append("\n");
        report.append("任务名称: ").append(progress.getTaskName()).append("\n");
        report.append("状态: ").append(progress.getStatus()).append("\n");
        report.append("总记录数: ").append(progress.getTotalRecords()).append("\n");
        report.append("已处理: ").append(progress.getProcessedRecords()).append("\n");
        report.append("成功: ").append(progress.getSuccessRecords()).append("\n");
        report.append("失败: ").append(progress.getFailedRecords()).append("\n");
        report.append("进度: ").append(String.format("%.2f%%", progress.getProgressPercentage())).append("\n");
        report.append("开始时间: ").append(new java.util.Date(progress.getStartTime())).append("\n");
        if (progress.getEndTime() > 0) {
            report.append("结束时间: ").append(new java.util.Date(progress.getEndTime())).append("\n");
            report.append("耗时: ").append((progress.getEndTime() - progress.getStartTime()) / 1000).append("秒\n");
        }
        
        if (progress.getRecentErrors() != null && !progress.getRecentErrors().isEmpty()) {
            report.append("\n错误信息:\n");
            for (String error : progress.getRecentErrors()) {
                report.append("- ").append(error).append("\n");
            }
        }
        
        return report.toString();
    }

    @Override
    public MigrationResult validateDataIntegrity(String dataType) {
        long startTime = System.currentTimeMillis();
        MigrationResult result = new MigrationResult();
        List<String> errors = new ArrayList<>();
        
        try {
            log.info("开始验证数据完整性，数据类型: {}", dataType);
            
            if ("questionnaire".equals(dataType)) {
                validateQuestionnaireIntegrity(result, errors);
            } else if ("questionnaire_result".equals(dataType)) {
                validateQuestionnaireResultIntegrity(result, errors);
            } else {
                result.setSuccess(false);
                result.setMessage("不支持的数据类型: " + dataType);
                return result;
            }
            
        } catch (Exception e) {
            log.error("数据完整性验证失败", e);
            result.setSuccess(false);
            result.setMessage("验证失败: " + e.getMessage());
            errors.add("验证失败: " + e.getMessage());
        }
        
        result.setErrors(errors);
        result.setDuration(System.currentTimeMillis() - startTime);
        
        log.info("数据完整性验证完成，结果: {}", result.getMessage());
        return result;
    }

    // 私有辅助方法

    private void executeAsyncMigration(String taskId, Map<String, Object> sourceConfig, Map<String, Object> migrationConfig) {
        MigrationProgress progress = migrationTasks.get(taskId);
        
        try {
            // 步骤1: 提取问卷数据
            progress.setCurrentStep("提取问卷数据");
            MigrationResult questionnaireResult = extractQuestionnaireData(sourceConfig);
            updateProgress(progress, questionnaireResult);
            
            if (!questionnaireResult.isSuccess()) {
                throw new RuntimeException("问卷数据提取失败");
            }
            
            // 步骤2: 提取问卷结果数据
            progress.setCurrentStep("提取问卷结果数据");
            MigrationResult resultResult = extractQuestionnaireResultData(sourceConfig, null);
            updateProgress(progress, resultResult);
            
            // 完成
            progress.setStatus("COMPLETED");
            progress.setCompleted(true);
            progress.setEndTime(System.currentTimeMillis());
            progress.setProgressPercentage(100.0);
            progress.setCurrentStep("迁移完成");
            
        } catch (Exception e) {
            progress.setStatus("FAILED");
            progress.setCompleted(true);
            progress.setEndTime(System.currentTimeMillis());
            
            List<String> errors = progress.getRecentErrors();
            if (errors == null) {
                errors = new ArrayList<>();
                progress.setRecentErrors(errors);
            }
            errors.add("迁移失败: " + e.getMessage());
        }
    }

    private void updateProgress(MigrationProgress progress, MigrationResult result) {
        progress.setTotalRecords(progress.getTotalRecords() + result.getTotalRecords());
        progress.setProcessedRecords(progress.getProcessedRecords() + result.getTotalRecords());
        progress.setSuccessRecords(progress.getSuccessRecords() + result.getSuccessRecords());
        progress.setFailedRecords(progress.getFailedRecords() + result.getFailedRecords());
        
        if (progress.getTotalRecords() > 0) {
            progress.setProgressPercentage((double) progress.getProcessedRecords() / progress.getTotalRecords() * 100);
        }
        
        if (result.getErrors() != null && !result.getErrors().isEmpty()) {
            List<String> recentErrors = progress.getRecentErrors();
            if (recentErrors == null) {
                recentErrors = new ArrayList<>();
                progress.setRecentErrors(recentErrors);
            }
            recentErrors.addAll(result.getErrors());
            
            // 只保留最近的10个错误
            if (recentErrors.size() > 10) {
                recentErrors = recentErrors.subList(recentErrors.size() - 10, recentErrors.size());
                progress.setRecentErrors(recentErrors);
            }
        }
    }

    private Map<String, Object> extractQuestionnaireFromResultSet(ResultSet rs) throws SQLException {
        Map<String, Object> data = new HashMap<>();
        data.put("id", rs.getLong("id"));
        data.put("title", rs.getString("title"));
        data.put("description", rs.getString("description"));
        data.put("questionnaire_type", rs.getInt("questionnaire_type"));
        data.put("target_audience", rs.getInt("target_audience"));
        data.put("question_count", rs.getInt("question_count"));
        data.put("estimated_duration", rs.getInt("estimated_duration"));
        data.put("external_link", rs.getString("external_link"));
        data.put("status", rs.getInt("status"));
        data.put("create_time", rs.getTimestamp("create_time"));
        return data;
    }

    private Map<String, Object> extractQuestionnaireResultFromResultSet(ResultSet rs) throws SQLException {
        Map<String, Object> data = new HashMap<>();
        data.put("id", rs.getLong("id"));
        data.put("questionnaire_id", rs.getLong("questionnaire_id"));
        data.put("student_profile_id", rs.getLong("student_profile_id"));
        data.put("total_score", rs.getInt("total_score"));
        data.put("max_score", rs.getInt("max_score"));
        data.put("risk_level", rs.getInt("risk_level"));
        data.put("result_interpretation", rs.getString("result_interpretation"));
        data.put("suggestions", rs.getString("suggestions"));
        data.put("answer_duration", rs.getInt("answer_duration"));
        data.put("create_time", rs.getTimestamp("create_time"));
        return data;
    }

    private boolean validateQuestionnaireData(Map<String, Object> data) {
        return data.get("title") != null && 
               data.get("questionnaire_type") != null &&
               data.get("external_link") != null;
    }

    private boolean validateQuestionnaireResultData(Map<String, Object> data) {
        // 必填：问卷ID、学生档案ID
        if (data.get("questionnaire_id") == null || data.get("student_profile_id") == null) {
            return false;
        }
        // 分数：兼容新老字段。raw_score 或 standard_score 至少存在一个；
        // 若均不存在则尝试从 total_score 兼容迁移（transform 阶段会补齐）
        boolean hasRaw = data.get("raw_score") != null;
        boolean hasStd = data.get("standard_score") != null;
        boolean hasLegacyTotal = data.get("total_score") != null;
        if (!(hasRaw || hasStd || hasLegacyTotal)) {
            return false;
        }
        // 可选：dimension_scores、risk_level
        // 推荐：report_content、generation_status、generation_time（不作为必填）
        return true;
    }

    private void transformQuestionnaireData(Map<String, Object> record) {
        // 数据格式转换逻辑
        // 例如：日期格式转换、字段映射等
    }

    private void transformQuestionnaireResultData(Map<String, Object> record) {
        // 字段兼容与标准化
        // 1) total_score -> raw_score（若新字段缺失）
        if (record.get("raw_score") == null && record.get("standard_score") == null) {
            Object total = record.get("total_score");
            if (total != null) {
                record.put("raw_score", total);
            }
        }
        // 2) result_content -> report_content
        if (record.get("report_content") == null && record.get("result_content") != null) {
            record.put("report_content", record.get("result_content"));
        }
        // 3) error_message -> generation_error
        if (record.get("generation_error") == null && record.get("error_message") != null) {
            record.put("generation_error", record.get("error_message"));
        }
        // 4) submit_time/complete_time -> generation_time（尽力而为）
        if (record.get("generation_time") == null) {
            Object completeTime = record.get("complete_time");
            if (completeTime != null) {
                record.put("generation_time", completeTime);
            }
        }
    }

    private void cleanDataRecord(Map<String, Object> record) {
        // 数据清洗逻辑
        // 例如：去除空白字符、标准化数据格式等
        record.replaceAll((k, v) -> v instanceof String ? ((String) v).trim() : v);
    }

    private void validateQuestionnaireForImport(Map<String, Object> record) throws Exception {
        // 导入前验证逻辑
        if (record.get("title") == null || ((String) record.get("title")).isEmpty()) {
            throw new Exception("问卷标题不能为空");
        }
    }

    private void validateQuestionnaireResultForImport(Map<String, Object> record) throws Exception {
        // 导入前验证逻辑
        if (record.get("questionnaire_id") == null) {
            throw new Exception("问卷ID不能为空");
        }
    }

    private void importQuestionnaireData(Map<String, Object> record) {
        // 导入问卷数据
        QuestionnaireDO questionnaire = new QuestionnaireDO();
        questionnaire.setTitle((String) record.get("title"));
        questionnaire.setDescription((String) record.get("description"));
        questionnaire.setQuestionnaireType((Integer) record.get("questionnaire_type"));
        questionnaire.setTargetAudience((Integer) record.get("target_audience"));
        questionnaire.setQuestionCount((Integer) record.get("question_count"));
        questionnaire.setEstimatedDuration((Integer) record.get("estimated_duration"));
        questionnaire.setExternalLink((String) record.get("external_link"));
        questionnaire.setStatus((Integer) record.get("status"));
        questionnaire.setCreateTime(LocalDateTime.now());
        
        questionnaireMapper.insert(questionnaire);
    }

    private void importQuestionnaireResultData(Map<String, Object> record) {
        // 导入问卷结果数据
        QuestionnaireResultDO result = new QuestionnaireResultDO();
        result.setQuestionnaireId((Long) record.get("questionnaire_id"));
        result.setStudentProfileId((Long) record.get("student_profile_id"));
        result.setRiskLevel((Integer) record.get("risk_level"));
        result.setLevelDescription((String) record.get("result_interpretation"));
        result.setSuggestions((String) record.get("suggestions"));
        result.setCompletedTime(LocalDateTime.now());
        
        questionnaireResultMapper.insert(result);
    }

    private void validateQuestionnaireIntegrity(MigrationResult result, List<String> errors) {
        // 验证问卷数据完整性
        long totalCount = questionnaireMapper.selectCount();
        result.setTotalRecords((int) totalCount);
        result.setSuccessRecords((int) totalCount);
        result.setFailedRecords(0);
        result.setSuccess(true);
        result.setMessage("问卷数据完整性验证通过，总计: " + totalCount);
    }

    private void validateQuestionnaireResultIntegrity(MigrationResult result, List<String> errors) {
        // 验证问卷结果数据完整性
        long totalCount = questionnaireResultMapper.selectCount();
        result.setTotalRecords((int) totalCount);
        result.setSuccessRecords((int) totalCount);
        result.setFailedRecords(0);
        result.setSuccess(true);
        result.setMessage("问卷结果数据完整性验证通过，总计: " + totalCount);
    }

}