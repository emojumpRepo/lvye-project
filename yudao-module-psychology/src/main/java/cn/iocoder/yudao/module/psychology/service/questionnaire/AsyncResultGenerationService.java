//package cn.iocoder.yudao.module.psychology.service.questionnaire;
//
//import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
//
///**
// * 异步结果生成服务接口
// *
// * @author 芋道源码
// */
//public interface AsyncResultGenerationService {
//
//    /**
//     * 异步生成问卷结果
//     *
//     * @param result 问卷结果对象
//     */
//    void generateResultAsync(QuestionnaireResultDO result);
//
//    /**
//     * 检查生成任务状态
//     *
//     * @param resultId 结果ID
//     * @return 任务状态
//     */
//    TaskStatus checkTaskStatus(Long resultId);
//
//    /**
//     * 取消生成任务
//     *
//     * @param resultId 结果ID
//     * @return 是否成功取消
//     */
//    boolean cancelTask(Long resultId);
//
//    /**
//     * 重试失败的生成任务
//     *
//     * @param resultId 结果ID
//     * @return 是否成功重试
//     */
//    boolean retryTask(Long resultId);
//
//    /**
//     * 任务状态枚举
//     */
//    enum TaskStatus {
//        PENDING("待处理"),
//        RUNNING("运行中"),
//        COMPLETED("已完成"),
//        FAILED("失败"),
//        CANCELLED("已取消");
//
//        private final String description;
//
//        TaskStatus(String description) {
//            this.description = description;
//        }
//
//        public String getDescription() {
//            return description;
//        }
//    }
//
//}