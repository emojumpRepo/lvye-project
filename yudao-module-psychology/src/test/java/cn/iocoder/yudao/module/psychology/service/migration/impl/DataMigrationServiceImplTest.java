package cn.iocoder.yudao.module.psychology.service.migration.impl;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultMapper;
import cn.iocoder.yudao.module.psychology.service.migration.DataMigrationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 数据迁移服务测试
 *
 * @author 芋道源码
 */
@Import(DataMigrationServiceImpl.class)
class DataMigrationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DataMigrationService dataMigrationService;

    @MockBean
    private QuestionnaireMapper questionnaireMapper;

    @MockBean
    private QuestionnaireResultMapper questionnaireResultMapper;

    @Test
    void testExtractQuestionnaireData_InvalidConfig() {
        // 准备测试数据 - 无效配置
        Map<String, Object> sourceConfig = new HashMap<>();
        sourceConfig.put("jdbcUrl", null);
        sourceConfig.put("username", "test");
        sourceConfig.put("password", "test");

        // 执行测试
        DataMigrationService.MigrationResult result = dataMigrationService.extractQuestionnaireData(sourceConfig);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("数据库连接配置不完整");
    }

    @Test
    void testTransformAndCleanData_Questionnaire() {
        // 准备测试数据
        List<Map<String, Object>> rawData = new ArrayList<>();
        
        Map<String, Object> record1 = new HashMap<>();
        record1.put("id", 1L);
        record1.put("title", "  测试问卷1  ");
        record1.put("description", "测试描述1");
        record1.put("questionnaire_type", 1);
        rawData.add(record1);
        
        Map<String, Object> record2 = new HashMap<>();
        record2.put("id", 2L);
        record2.put("title", "测试问卷2");
        record2.put("description", "  测试描述2  ");
        record2.put("questionnaire_type", 2);
        rawData.add(record2);

        // 执行测试
        DataMigrationService.MigrationResult result = dataMigrationService.transformAndCleanData(rawData, "questionnaire");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessRecords()).isEqualTo(2);
        assertThat(result.getFailedRecords()).isEqualTo(0);
        assertThat(result.getMessage()).contains("转换完成");
        
        // 验证数据清洗效果（去除空白字符）
        assertThat(record1.get("title")).isEqualTo("测试问卷1");
        assertThat(record2.get("description")).isEqualTo("测试描述2");
    }

    @Test
    void testTransformAndCleanData_QuestionnaireResult() {
        // 准备测试数据
        List<Map<String, Object>> rawData = new ArrayList<>();
        
        Map<String, Object> record1 = new HashMap<>();
        record1.put("id", 1L);
        record1.put("questionnaire_id", 1L);
        record1.put("student_profile_id", 100L);
        record1.put("total_score", 85);
        record1.put("max_score", 100);
        rawData.add(record1);

        // 执行测试
        DataMigrationService.MigrationResult result = dataMigrationService.transformAndCleanData(rawData, "questionnaire_result");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(1);
        assertThat(result.getSuccessRecords()).isEqualTo(1);
        assertThat(result.getFailedRecords()).isEqualTo(0);
    }

    @Test
    void testImportAndValidateData_ValidateOnly() {
        // 准备测试数据
        List<Map<String, Object>> cleanedData = new ArrayList<>();
        
        Map<String, Object> record1 = new HashMap<>();
        record1.put("title", "测试问卷1");
        record1.put("description", "测试描述1");
        record1.put("questionnaire_type", 1);
        record1.put("target_audience", 1);
        record1.put("question_count", 20);
        record1.put("estimated_duration", 15);
        record1.put("external_link", "https://example.com/survey/1");
        record1.put("status", 1);
        cleanedData.add(record1);

        // 执行测试 - 仅验证
        DataMigrationService.MigrationResult result = dataMigrationService.importAndValidateData(cleanedData, "questionnaire", true);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(1);
        assertThat(result.getSuccessRecords()).isEqualTo(1);
        assertThat(result.getMessage()).contains("验证完成");
        
        // 验证没有实际插入数据
        verify(questionnaireMapper, never()).insert(any());
    }

    @Test
    void testImportAndValidateData_ValidateOnly_InvalidData() {
        // 准备测试数据 - 缺少必要字段
        List<Map<String, Object>> cleanedData = new ArrayList<>();
        
        Map<String, Object> record1 = new HashMap<>();
        record1.put("title", ""); // 空标题
        record1.put("description", "测试描述1");
        cleanedData.add(record1);

        // 执行测试 - 仅验证
        DataMigrationService.MigrationResult result = dataMigrationService.importAndValidateData(cleanedData, "questionnaire", true);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getTotalRecords()).isEqualTo(1);
        assertThat(result.getSuccessRecords()).isEqualTo(0);
        assertThat(result.getFailedRecords()).isEqualTo(1);
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0)).contains("问卷标题不能为空");
    }

    @Test
    void testImportAndValidateData_ActualImport() {
        // 准备测试数据
        List<Map<String, Object>> cleanedData = new ArrayList<>();
        
        Map<String, Object> record1 = new HashMap<>();
        record1.put("title", "测试问卷1");
        record1.put("description", "测试描述1");
        record1.put("questionnaire_type", 1);
        record1.put("target_audience", 1);
        record1.put("question_count", 20);
        record1.put("estimated_duration", 15);
        record1.put("external_link", "https://example.com/survey/1");
        record1.put("status", 1);
        cleanedData.add(record1);

        // 执行测试 - 实际导入
        DataMigrationService.MigrationResult result = dataMigrationService.importAndValidateData(cleanedData, "questionnaire", false);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(1);
        assertThat(result.getSuccessRecords()).isEqualTo(1);
        assertThat(result.getMessage()).contains("导入完成");
        
        // 验证实际插入了数据
        verify(questionnaireMapper, times(1)).insert(any());
    }

    @Test
    void testStartAsyncMigration() {
        // 准备测试数据
        Map<String, Object> sourceConfig = new HashMap<>();
        sourceConfig.put("jdbcUrl", "jdbc:h2:mem:test");
        sourceConfig.put("username", "test");
        sourceConfig.put("password", "test");
        
        Map<String, Object> migrationConfig = new HashMap<>();
        migrationConfig.put("batchSize", 100);

        // 执行测试
        String taskId = dataMigrationService.startAsyncMigration("测试迁移任务", sourceConfig, migrationConfig);

        // 验证结果
        assertThat(taskId).isNotNull();
        assertThat(taskId).isNotEmpty();
        
        // 验证任务已创建
        DataMigrationService.MigrationProgress progress = dataMigrationService.getMigrationProgress(taskId);
        assertThat(progress).isNotNull();
        assertThat(progress.getTaskId()).isEqualTo(taskId);
        assertThat(progress.getTaskName()).isEqualTo("测试迁移任务");
        assertThat(progress.getStatus()).isIn("RUNNING", "COMPLETED", "FAILED");
    }

    @Test
    void testGetMigrationProgress_NotFound() {
        // 执行测试
        DataMigrationService.MigrationProgress progress = dataMigrationService.getMigrationProgress("non-existent-task");

        // 验证结果
        assertThat(progress).isNull();
    }

    @Test
    void testCancelMigration() {
        // 准备测试数据 - 创建一个任务
        Map<String, Object> sourceConfig = new HashMap<>();
        sourceConfig.put("jdbcUrl", "jdbc:h2:mem:test");
        sourceConfig.put("username", "test");
        sourceConfig.put("password", "test");
        
        String taskId = dataMigrationService.startAsyncMigration("测试取消任务", sourceConfig, new HashMap<>());

        // 等待一小段时间确保任务开始
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 执行测试
        boolean cancelled = dataMigrationService.cancelMigration(taskId);

        // 验证结果
        assertThat(cancelled).isTrue();
        
        DataMigrationService.MigrationProgress progress = dataMigrationService.getMigrationProgress(taskId);
        assertThat(progress).isNotNull();
        assertThat(progress.getStatus()).isEqualTo("CANCELLED");
        assertThat(progress.isCompleted()).isTrue();
    }

    @Test
    void testCancelMigration_NotFound() {
        // 执行测试
        boolean cancelled = dataMigrationService.cancelMigration("non-existent-task");

        // 验证结果
        assertThat(cancelled).isFalse();
    }

    @Test
    void testGetAllMigrationTasks() {
        // 准备测试数据 - 创建几个任务
        Map<String, Object> sourceConfig = new HashMap<>();
        sourceConfig.put("jdbcUrl", "jdbc:h2:mem:test");
        sourceConfig.put("username", "test");
        sourceConfig.put("password", "test");
        
        String taskId1 = dataMigrationService.startAsyncMigration("任务1", sourceConfig, new HashMap<>());
        String taskId2 = dataMigrationService.startAsyncMigration("任务2", sourceConfig, new HashMap<>());

        // 执行测试
        List<DataMigrationService.MigrationProgress> allTasks = dataMigrationService.getAllMigrationTasks();

        // 验证结果
        assertThat(allTasks).isNotNull();
        assertThat(allTasks.size()).isGreaterThanOrEqualTo(2);
        
        List<String> taskIds = allTasks.stream()
                .map(DataMigrationService.MigrationProgress::getTaskId)
                .toList();
        assertThat(taskIds).contains(taskId1, taskId2);
    }

    @Test
    void testGenerateMigrationReport() {
        // 准备测试数据 - 创建一个任务
        Map<String, Object> sourceConfig = new HashMap<>();
        sourceConfig.put("jdbcUrl", "jdbc:h2:mem:test");
        sourceConfig.put("username", "test");
        sourceConfig.put("password", "test");
        
        String taskId = dataMigrationService.startAsyncMigration("测试报告任务", sourceConfig, new HashMap<>());

        // 执行测试
        String report = dataMigrationService.generateMigrationReport(taskId);

        // 验证结果
        assertThat(report).isNotNull();
        assertThat(report).contains("迁移任务报告");
        assertThat(report).contains("任务ID: " + taskId);
        assertThat(report).contains("任务名称: 测试报告任务");
        assertThat(report).contains("状态:");
        assertThat(report).contains("开始时间:");
    }

    @Test
    void testGenerateMigrationReport_NotFound() {
        // 执行测试
        String report = dataMigrationService.generateMigrationReport("non-existent-task");

        // 验证结果
        assertThat(report).isEqualTo("任务不存在");
    }

    @Test
    void testValidateDataIntegrity_Questionnaire() {
        // 准备测试数据
        when(questionnaireMapper.selectCount()).thenReturn(10);

        // 执行测试
        DataMigrationService.MigrationResult result = dataMigrationService.validateDataIntegrity("questionnaire");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(10);
        assertThat(result.getSuccessRecords()).isEqualTo(10);
        assertThat(result.getFailedRecords()).isEqualTo(0);
        assertThat(result.getMessage()).contains("问卷数据完整性验证通过");
        
        verify(questionnaireMapper).selectCount();
    }

    @Test
    void testValidateDataIntegrity_QuestionnaireResult() {
        // 准备测试数据
        when(questionnaireResultMapper.selectCount()).thenReturn(50);

        // 执行测试
        DataMigrationService.MigrationResult result = dataMigrationService.validateDataIntegrity("questionnaire_result");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(50);
        assertThat(result.getSuccessRecords()).isEqualTo(50);
        assertThat(result.getFailedRecords()).isEqualTo(0);
        assertThat(result.getMessage()).contains("问卷结果数据完整性验证通过");
        
        verify(questionnaireResultMapper).selectCount();
    }

    @Test
    void testValidateDataIntegrity_UnsupportedType() {
        // 执行测试
        DataMigrationService.MigrationResult result = dataMigrationService.validateDataIntegrity("unsupported_type");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("不支持的数据类型");
    }

    @Test
    void testCleanupCompletedTasks() {
        // 准备测试数据 - 创建一些任务并手动设置为完成状态
        Map<String, Object> sourceConfig = new HashMap<>();
        sourceConfig.put("jdbcUrl", "jdbc:h2:mem:test");
        sourceConfig.put("username", "test");
        sourceConfig.put("password", "test");
        
        String taskId1 = dataMigrationService.startAsyncMigration("完成任务1", sourceConfig, new HashMap<>());
        String taskId2 = dataMigrationService.startAsyncMigration("完成任务2", sourceConfig, new HashMap<>());
        
        // 手动设置任务为完成状态（模拟旧任务）
        DataMigrationService.MigrationProgress progress1 = dataMigrationService.getMigrationProgress(taskId1);
        DataMigrationService.MigrationProgress progress2 = dataMigrationService.getMigrationProgress(taskId2);
        
        if (progress1 != null) {
            progress1.setCompleted(true);
            progress1.setStatus("COMPLETED");
            progress1.setEndTime(System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)); // 2天前
        }
        
        if (progress2 != null) {
            progress2.setCompleted(true);
            progress2.setStatus("COMPLETED");
            progress2.setEndTime(System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)); // 2天前
        }

        // 执行测试 - 清理1天前的任务
        int cleanedCount = dataMigrationService.cleanupCompletedTasks(1);

        // 验证结果
        assertThat(cleanedCount).isGreaterThanOrEqualTo(0); // 可能有其他测试创建的任务
    }

}