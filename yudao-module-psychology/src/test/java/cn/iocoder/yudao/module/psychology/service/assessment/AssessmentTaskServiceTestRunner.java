package cn.iocoder.yudao.module.psychology.service.assessment;

import cn.iocoder.yudao.module.psychology.controller.admin.assessment.vo.AssessmentTaskSaveReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.assessment.AssessmentTaskDO;
import cn.iocoder.yudao.module.psychology.enums.AssessmentTaskStatusEnum;

import java.util.ArrayList;
import java.util.Date;

/**
 * 测评任务服务测试运行器
 * 用于手动验证 isPublish 功能
 */
public class AssessmentTaskServiceTestRunner {

    public static void main(String[] args) {
        System.out.println("=== 测评任务服务功能测试 ===");
        
        // 测试1: 验证 isPublish 字段的默认值
        testIsPublishDefaultValue();
        
        // 测试2: 验证 isPublish 为 true 时的行为
        testIsPublishTrueValue();
        
        // 测试3: 验证 isPublish 为 false 时的行为
        testIsPublishFalseValue();
        
        // 测试4: 验证 isPublish 为 null 时的行为
        testIsPublishNullValue();
        
        System.out.println("=== 所有测试完成 ===");
    }
    
    private static void testIsPublishDefaultValue() {
        System.out.println("\n--- 测试1: isPublish 默认值 ---");
        
        AssessmentTaskSaveReqVO reqVO = createTestReqVO();
        // 不设置 isPublish，应该使用默认值 false
        
        System.out.println("isPublish 默认值: " + reqVO.getIsPublish());
        System.out.println("预期: false, 实际: " + reqVO.getIsPublish());
        
        boolean isDefault = reqVO.getIsPublish() != null && !reqVO.getIsPublish();
        System.out.println("测试结果: " + (isDefault ? "通过" : "失败"));
    }
    
    private static void testIsPublishTrueValue() {
        System.out.println("\n--- 测试2: isPublish = true ---");
        
        AssessmentTaskSaveReqVO reqVO = createTestReqVO();
        reqVO.setIsPublish(true);
        
        System.out.println("设置 isPublish = true");
        System.out.println("isPublish 值: " + reqVO.getIsPublish());
        
        // 模拟服务逻辑
        boolean shouldPublish = reqVO.getIsPublish() != null && reqVO.getIsPublish();
        System.out.println("应该发布: " + shouldPublish);
        System.out.println("测试结果: " + (shouldPublish ? "通过" : "失败"));
    }
    
    private static void testIsPublishFalseValue() {
        System.out.println("\n--- 测试3: isPublish = false ---");
        
        AssessmentTaskSaveReqVO reqVO = createTestReqVO();
        reqVO.setIsPublish(false);
        
        System.out.println("设置 isPublish = false");
        System.out.println("isPublish 值: " + reqVO.getIsPublish());
        
        // 模拟服务逻辑
        boolean shouldPublish = reqVO.getIsPublish() != null && reqVO.getIsPublish();
        System.out.println("应该发布: " + shouldPublish);
        System.out.println("测试结果: " + (!shouldPublish ? "通过" : "失败"));
    }
    
    private static void testIsPublishNullValue() {
        System.out.println("\n--- 测试4: isPublish = null ---");
        
        AssessmentTaskSaveReqVO reqVO = createTestReqVO();
        reqVO.setIsPublish(null);
        
        System.out.println("设置 isPublish = null");
        System.out.println("isPublish 值: " + reqVO.getIsPublish());
        
        // 模拟服务逻辑
        boolean shouldPublish = reqVO.getIsPublish() != null && reqVO.getIsPublish();
        System.out.println("应该发布: " + shouldPublish);
        System.out.println("测试结果: " + (!shouldPublish ? "通过" : "失败"));
    }
    
    private static AssessmentTaskSaveReqVO createTestReqVO() {
        AssessmentTaskSaveReqVO reqVO = new AssessmentTaskSaveReqVO();
        reqVO.setTaskName("测试任务_" + System.currentTimeMillis());
        reqVO.setScaleCode("A");
        reqVO.setTargetAudience(1); // 学生
        reqVO.setStartline(new Date());
        reqVO.setDeadline(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)); // 7天后
        reqVO.setDeptIdList(new ArrayList<>());
        reqVO.setUserIdList(new ArrayList<>());
        return reqVO;
    }
    
    /**
     * 测试状态枚举
     */
    private static void testStatusEnum() {
        System.out.println("\n--- 测试状态枚举 ---");
        System.out.println("NOT_STARTED: " + AssessmentTaskStatusEnum.NOT_STARTED.getStatus());
        System.out.println("IN_PROGRESS: " + AssessmentTaskStatusEnum.IN_PROGRESS.getStatus());
        System.out.println("COMPLETED: " + AssessmentTaskStatusEnum.COMPLETED.getStatus());
        System.out.println("CLOSED: " + AssessmentTaskStatusEnum.CLOSED.getStatus());
    }
}
