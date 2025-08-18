package cn.iocoder.yudao.module.psychology.convert.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.*;
import cn.iocoder.yudao.module.psychology.controller.app.questionnaire.vo.AppQuestionnaireSimpleRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireDO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 问卷转换器测试
 *
 * @author 芋道源码
 */
class QuestionnaireConvertTest extends BaseDbUnitTest {

    @Test
    void testConvertFromCreateReqVO() {
        // 准备测试数据
        QuestionnaireCreateReqVO createReqVO = new QuestionnaireCreateReqVO();
        createReqVO.setTitle("测试问卷");
        createReqVO.setDescription("测试问卷描述");
        createReqVO.setQuestionnaireType(1);
        createReqVO.setTargetAudience(1);
        createReqVO.setQuestionCount(20);
        createReqVO.setEstimatedDuration(15);
        createReqVO.setExternalLink("https://example.com/survey/123");
        createReqVO.setRemark("测试备注");

        // 执行转换
        QuestionnaireDO questionnaireDO = QuestionnaireConvert.INSTANCE.convert(createReqVO);

        // 验证结果
        assertThat(questionnaireDO).isNotNull();
        assertThat(questionnaireDO.getTitle()).isEqualTo("测试问卷");
        assertThat(questionnaireDO.getDescription()).isEqualTo("测试问卷描述");
        assertThat(questionnaireDO.getQuestionnaireType()).isEqualTo(1);
        assertThat(questionnaireDO.getTargetAudience()).isEqualTo(1);
        assertThat(questionnaireDO.getQuestionCount()).isEqualTo(20);
        assertThat(questionnaireDO.getEstimatedDuration()).isEqualTo(15);
        assertThat(questionnaireDO.getExternalLink()).isEqualTo("https://example.com/survey/123");
        assertThat(questionnaireDO.getRemark()).isEqualTo("测试备注");
        
        // 这些字段应该被忽略
        assertThat(questionnaireDO.getId()).isNull();
        assertThat(questionnaireDO.getStatus()).isNull();
        assertThat(questionnaireDO.getIsOpen()).isNull();
        assertThat(questionnaireDO.getAccessCount()).isNull();
        assertThat(questionnaireDO.getCompletionCount()).isNull();
    }

    @Test
    void testConvertFromUpdateReqVO() {
        // 准备测试数据
        QuestionnaireUpdateReqVO updateReqVO = new QuestionnaireUpdateReqVO();
        updateReqVO.setId(1L);
        updateReqVO.setTitle("更新测试问卷");
        updateReqVO.setDescription("更新测试问卷描述");
        updateReqVO.setQuestionnaireType(2);
        updateReqVO.setTargetAudience(2);
        updateReqVO.setQuestionCount(25);
        updateReqVO.setEstimatedDuration(20);
        updateReqVO.setExternalLink("https://example.com/survey/456");
        updateReqVO.setRemark("更新测试备注");

        // 执行转换
        QuestionnaireDO questionnaireDO = QuestionnaireConvert.INSTANCE.convert(updateReqVO);

        // 验证结果
        assertThat(questionnaireDO).isNotNull();
        assertThat(questionnaireDO.getId()).isEqualTo(1L);
        assertThat(questionnaireDO.getTitle()).isEqualTo("更新测试问卷");
        assertThat(questionnaireDO.getDescription()).isEqualTo("更新测试问卷描述");
        assertThat(questionnaireDO.getQuestionnaireType()).isEqualTo(2);
        assertThat(questionnaireDO.getTargetAudience()).isEqualTo(2);
        assertThat(questionnaireDO.getQuestionCount()).isEqualTo(25);
        assertThat(questionnaireDO.getEstimatedDuration()).isEqualTo(20);
        assertThat(questionnaireDO.getExternalLink()).isEqualTo("https://example.com/survey/456");
        assertThat(questionnaireDO.getRemark()).isEqualTo("更新测试备注");
    }

    @Test
    void testConvertToRespVO() {
        // 准备测试数据
        QuestionnaireDO questionnaireDO = createTestQuestionnaireDO();

        // 执行转换
        QuestionnaireRespVO respVO = QuestionnaireConvert.INSTANCE.convert(questionnaireDO);

        // 验证结果
        assertThat(respVO).isNotNull();
        assertThat(respVO.getId()).isEqualTo(1L);
        assertThat(respVO.getTitle()).isEqualTo("测试问卷");
        assertThat(respVO.getDescription()).isEqualTo("测试问卷描述");
        assertThat(respVO.getQuestionnaireType()).isEqualTo(1);
        assertThat(respVO.getTargetAudience()).isEqualTo(1);
        assertThat(respVO.getQuestionCount()).isEqualTo(20);
        assertThat(respVO.getEstimatedDuration()).isEqualTo(15);
        assertThat(respVO.getExternalLink()).isEqualTo("https://example.com/survey/123");
        assertThat(respVO.getStatus()).isEqualTo(1);
        assertThat(respVO.getIsOpen()).isEqualTo(true);
        assertThat(respVO.getAccessCount()).isEqualTo(100);
        assertThat(respVO.getCompletionCount()).isEqualTo(80);
    }

    @Test
    void testConvertToSimpleRespVO() {
        // 准备测试数据
        QuestionnaireDO questionnaireDO = createTestQuestionnaireDO();

        // 执行转换
        QuestionnaireSimpleRespVO simpleRespVO = QuestionnaireConvert.INSTANCE.convertSimple(questionnaireDO);

        // 验证结果
        assertThat(simpleRespVO).isNotNull();
        assertThat(simpleRespVO.getId()).isEqualTo(1L);
        assertThat(simpleRespVO.getTitle()).isEqualTo("测试问卷");
        assertThat(simpleRespVO.getQuestionnaireType()).isEqualTo(1);
        assertThat(simpleRespVO.getTargetAudience()).isEqualTo(1);
        assertThat(simpleRespVO.getStatus()).isEqualTo(1);
    }

    @Test
    void testConvertToAppSimpleRespVO() {
        // 准备测试数据
        QuestionnaireDO questionnaireDO = createTestQuestionnaireDO();

        // 执行转换
        AppQuestionnaireSimpleRespVO appSimpleRespVO = QuestionnaireConvert.INSTANCE.convertToAppSimpleRespVO(questionnaireDO);

        // 验证结果
        assertThat(appSimpleRespVO).isNotNull();
        assertThat(appSimpleRespVO.getId()).isEqualTo(1L);
        assertThat(appSimpleRespVO.getTitle()).isEqualTo("测试问卷");
        assertThat(appSimpleRespVO.getDescription()).isEqualTo("测试问卷描述");
        assertThat(appSimpleRespVO.getQuestionnaireType()).isEqualTo(1);
        assertThat(appSimpleRespVO.getQuestionCount()).isEqualTo(20);
        assertThat(appSimpleRespVO.getEstimatedDuration()).isEqualTo(15);
        assertThat(appSimpleRespVO.getExternalLink()).isEqualTo("https://example.com/survey/123");
        
        // 这些字段需要手动设置，转换器会忽略
        assertThat(appSimpleRespVO.getCompleted()).isNull();
        assertThat(appSimpleRespVO.getAccessible()).isNull();
    }

    @Test
    void testConvertList() {
        // 准备测试数据
        List<QuestionnaireDO> questionnaireDOList = Arrays.asList(
                createTestQuestionnaireDO(),
                createTestQuestionnaireDO()
        );

        // 执行转换
        List<QuestionnaireRespVO> respVOList = QuestionnaireConvert.INSTANCE.convertList(questionnaireDOList);

        // 验证结果
        assertThat(respVOList).isNotNull();
        assertThat(respVOList).hasSize(2);
        assertThat(respVOList.get(0).getId()).isEqualTo(1L);
        assertThat(respVOList.get(1).getId()).isEqualTo(1L);
    }

    @Test
    void testConvertToAppSimpleRespVOList() {
        // 准备测试数据
        List<QuestionnaireDO> questionnaireDOList = Arrays.asList(
                createTestQuestionnaireDO(),
                createTestQuestionnaireDO()
        );

        // 执行转换
        List<AppQuestionnaireSimpleRespVO> appSimpleRespVOList = QuestionnaireConvert.INSTANCE.convertToAppSimpleRespVOList(questionnaireDOList);

        // 验证结果
        assertThat(appSimpleRespVOList).isNotNull();
        assertThat(appSimpleRespVOList).hasSize(2);
        assertThat(appSimpleRespVOList.get(0).getId()).isEqualTo(1L);
        assertThat(appSimpleRespVOList.get(1).getId()).isEqualTo(1L);
    }

    @Test
    void testConvertPage() {
        // 准备测试数据
        List<QuestionnaireDO> questionnaireDOList = Arrays.asList(
                createTestQuestionnaireDO(),
                createTestQuestionnaireDO()
        );
        PageResult<QuestionnaireDO> pageResult = new PageResult<>(questionnaireDOList, 2L);

        // 执行转换
        PageResult<QuestionnaireRespVO> respVOPageResult = QuestionnaireConvert.INSTANCE.convertPage(pageResult);

        // 验证结果
        assertThat(respVOPageResult).isNotNull();
        assertThat(respVOPageResult.getList()).hasSize(2);
        assertThat(respVOPageResult.getTotal()).isEqualTo(2L);
        assertThat(respVOPageResult.getList().get(0).getId()).isEqualTo(1L);
    }

    // 辅助方法

    private QuestionnaireDO createTestQuestionnaireDO() {
        QuestionnaireDO questionnaireDO = new QuestionnaireDO();
        questionnaireDO.setId(1L);
        questionnaireDO.setTitle("测试问卷");
        questionnaireDO.setDescription("测试问卷描述");
        questionnaireDO.setQuestionnaireType(1);
        questionnaireDO.setTargetAudience(1);
        questionnaireDO.setQuestionCount(20);
        questionnaireDO.setEstimatedDuration(15);
        questionnaireDO.setExternalLink("https://example.com/survey/123");
        questionnaireDO.setStatus(1);
        questionnaireDO.setIsOpen(true);
        questionnaireDO.setAccessCount(100);
        questionnaireDO.setCompletionCount(80);
        questionnaireDO.setSyncStatus(1);
        questionnaireDO.setRemark("测试备注");
        questionnaireDO.setCreateTime(LocalDateTime.now());
        return questionnaireDO;
    }

}