package cn.iocoder.yudao.module.psychology.convert.questionnaireresult;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaireresult.vo.*;
import cn.iocoder.yudao.module.psychology.controller.app.questionnaireresult.vo.AppQuestionnaireResultRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultDO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 问卷结果转换器测试
 *
 * @author 芋道源码
 */
class QuestionnaireResultConvertTest extends BaseDbUnitTest {

    @Test
    void testConvert() {
        // 准备测试数据
        QuestionnaireResultSaveReqVO saveReqVO = new QuestionnaireResultSaveReqVO();
        saveReqVO.setQuestionnaireId(1L);
        saveReqVO.setStudentProfileId(100L);
        saveReqVO.setTotalScore(85);
        saveReqVO.setMaxScore(100);
        saveReqVO.setRiskLevel(2);
        saveReqVO.setResultInterpretation("测试解读");
        saveReqVO.setSuggestions("测试建议");
        saveReqVO.setAnswerDuration(900);

        // 执行转换
        QuestionnaireResultDO resultDO = QuestionnaireResultConvert.INSTANCE.convert(saveReqVO);

        // 验证结果
        assertThat(resultDO).isNotNull();
        assertThat(resultDO.getQuestionnaireId()).isEqualTo(1L);
        assertThat(resultDO.getStudentProfileId()).isEqualTo(100L);
        assertThat(resultDO.getTotalScore()).isEqualTo(85);
        assertThat(resultDO.getMaxScore()).isEqualTo(100);
        assertThat(resultDO.getRiskLevel()).isEqualTo(2);
        assertThat(resultDO.getResultInterpretation()).isEqualTo("测试解读");
        assertThat(resultDO.getSuggestions()).isEqualTo("测试建议");
        assertThat(resultDO.getAnswerDuration()).isEqualTo(900);
    }

    @Test
    void testConvertToRespVO() {
        // 准备测试数据
        QuestionnaireResultDO resultDO = createTestResultDO();

        // 执行转换
        QuestionnaireResultRespVO respVO = QuestionnaireResultConvert.INSTANCE.convert(resultDO);

        // 验证结果
        assertThat(respVO).isNotNull();
        assertThat(respVO.getId()).isEqualTo(2001L);
        assertThat(respVO.getQuestionnaireId()).isEqualTo(1L);
        assertThat(respVO.getStudentProfileId()).isEqualTo(100L);
        assertThat(respVO.getTotalScore()).isEqualTo(85);
        assertThat(respVO.getMaxScore()).isEqualTo(100);
        assertThat(respVO.getRiskLevel()).isEqualTo(2);
        assertThat(respVO.getResultInterpretation()).isEqualTo("测试解读");
        assertThat(respVO.getSuggestions()).isEqualTo("测试建议");
        assertThat(respVO.getAnswerDuration()).isEqualTo(900);
    }

    @Test
    void testConvertToAppRespVO() {
        // 准备测试数据
        QuestionnaireResultDO resultDO = createTestResultDO();

        // 执行转换
        AppQuestionnaireResultRespVO appRespVO = QuestionnaireResultConvert.INSTANCE.convertToAppRespVO(resultDO);

        // 验证结果
        assertThat(appRespVO).isNotNull();
        assertThat(appRespVO.getId()).isEqualTo(2001L);
        assertThat(appRespVO.getQuestionnaireId()).isEqualTo(1L);
        assertThat(appRespVO.getStudentProfileId()).isEqualTo(100L);
        assertThat(appRespVO.getTotalScore()).isEqualTo(85);
        assertThat(appRespVO.getMaxScore()).isEqualTo(100);
        assertThat(appRespVO.getRiskLevel()).isEqualTo(2);
        assertThat(appRespVO.getResultInterpretation()).isEqualTo("测试解读");
        assertThat(appRespVO.getSuggestions()).isEqualTo("测试建议");
        assertThat(appRespVO.getAnswerDuration()).isEqualTo(900);
        assertThat(appRespVO.getGenerateTime()).isEqualTo(resultDO.getCreateTime());
        
        // 这些字段需要手动设置，转换器会忽略
        assertThat(appRespVO.getQuestionnaireTitle()).isNull();
        assertThat(appRespVO.getScoreRate()).isNull();
        assertThat(appRespVO.getRiskLevelDesc()).isNull();
        assertThat(appRespVO.getCanRetake()).isNull();
    }

    @Test
    void testConvertToSimpleRespVO() {
        // 准备测试数据
        QuestionnaireResultDO resultDO = createTestResultDO();

        // 执行转换
        QuestionnaireResultSimpleRespVO simpleRespVO = QuestionnaireResultConvert.INSTANCE.convertToSimpleRespVO(resultDO);

        // 验证结果
        assertThat(simpleRespVO).isNotNull();
        assertThat(simpleRespVO.getId()).isEqualTo(2001L);
        assertThat(simpleRespVO.getQuestionnaireId()).isEqualTo(1L);
        assertThat(simpleRespVO.getStudentProfileId()).isEqualTo(100L);
        assertThat(simpleRespVO.getTotalScore()).isEqualTo(85);
        assertThat(simpleRespVO.getMaxScore()).isEqualTo(100);
        assertThat(simpleRespVO.getRiskLevel()).isEqualTo(2);
        assertThat(simpleRespVO.getResultStatus()).isEqualTo(2);
        assertThat(simpleRespVO.getCompleteTime()).isEqualTo(resultDO.getCompleteTime());
    }

    @Test
    void testConvertToExportRespVO() {
        // 准备测试数据
        QuestionnaireResultDO resultDO = createTestResultDO();

        // 执行转换
        QuestionnaireResultExportRespVO exportRespVO = QuestionnaireResultConvert.INSTANCE.convertToExportRespVO(resultDO);

        // 验证结果
        assertThat(exportRespVO).isNotNull();
        assertThat(exportRespVO.getId()).isEqualTo(2001L);
        assertThat(exportRespVO.getQuestionnaireId()).isEqualTo(1L);
        assertThat(exportRespVO.getStudentProfileId()).isEqualTo(100L);
        assertThat(exportRespVO.getTotalScore()).isEqualTo(85);
        assertThat(exportRespVO.getMaxScore()).isEqualTo(100);
        assertThat(exportRespVO.getRiskLevel()).isEqualTo(2);
        assertThat(exportRespVO.getResultStatus()).isEqualTo(2);
        assertThat(exportRespVO.getCompleteTime()).isEqualTo(resultDO.getCompleteTime());
    }

    @Test
    void testConvertList() {
        // 准备测试数据
        List<QuestionnaireResultDO> resultDOList = Arrays.asList(
                createTestResultDO(),
                createTestResultDO()
        );

        // 执行转换
        List<QuestionnaireResultRespVO> respVOList = QuestionnaireResultConvert.INSTANCE.convertList(resultDOList);

        // 验证结果
        assertThat(respVOList).isNotNull();
        assertThat(respVOList).hasSize(2);
        assertThat(respVOList.get(0).getId()).isEqualTo(2001L);
        assertThat(respVOList.get(1).getId()).isEqualTo(2001L);
    }

    @Test
    void testConvertPage() {
        // 准备测试数据
        List<QuestionnaireResultDO> resultDOList = Arrays.asList(
                createTestResultDO(),
                createTestResultDO()
        );
        PageResult<QuestionnaireResultDO> pageResult = new PageResult<>(resultDOList, 2L);

        // 执行转换
        PageResult<QuestionnaireResultRespVO> respVOPageResult = QuestionnaireResultConvert.INSTANCE.convertPage(pageResult);

        // 验证结果
        assertThat(respVOPageResult).isNotNull();
        assertThat(respVOPageResult.getList()).hasSize(2);
        assertThat(respVOPageResult.getTotal()).isEqualTo(2L);
        assertThat(respVOPageResult.getList().get(0).getId()).isEqualTo(2001L);
    }

    // 辅助方法

    private QuestionnaireResultDO createTestResultDO() {
        QuestionnaireResultDO resultDO = new QuestionnaireResultDO();
        resultDO.setId(2001L);
        resultDO.setQuestionnaireId(1L);
        resultDO.setStudentProfileId(100L);
        resultDO.setTotalScore(85);
        resultDO.setMaxScore(100);
        resultDO.setRiskLevel(2);
        resultDO.setResultStatus(2);
        resultDO.setResultInterpretation("测试解读");
        resultDO.setSuggestions("测试建议");
        resultDO.setDetailedReport("详细报告");
        resultDO.setAnswerDuration(900);
        resultDO.setCreateTime(LocalDateTime.now().minusHours(1));
        resultDO.setCompleteTime(LocalDateTime.now());
        return resultDO;
    }

}