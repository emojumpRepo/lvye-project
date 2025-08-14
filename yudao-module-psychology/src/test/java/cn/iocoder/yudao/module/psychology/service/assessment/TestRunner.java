package cn.iocoder.yudao.module.psychology.service.assessment;

/**
 * 简单的测试运行器
 */
public class TestRunner {
    
    public static void main(String[] args) {
        System.out.println("=== 开始运行测评任务服务测试 ===\n");
        
        try {
            AssessmentTaskServiceImplTest test = new AssessmentTaskServiceImplTest();
            
            // 运行所有测试方法
            test.testIsPublishField_defaultValue();
            test.testIsPublishField_trueValue();
            test.testIsPublishField_falseValue();
            test.testIsPublishField_nullValue();
            test.testServiceLogic_publishDecision();
            test.testStatusEnum_values();
            
            System.out.println("\n=== 所有测试完成 ===");
            System.out.println("✅ 测试结果: 全部通过");
            
        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
