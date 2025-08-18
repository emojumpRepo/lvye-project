package cn.iocoder.yudao.module.psychology.convert.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.psychology.controller.app.questionnaire.vo.AppQuestionnaireAccessRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireAccessDO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 问卷访问记录转换器测试
 *
 * @author 芋道源码
 */
class QuestionnaireAccessConvertTest extends BaseDbUnitTest {

    @Test
    void testConvertToAppRespVO() {
        // 准备测试数据
        QuestionnaireAccessDO accessDO = createTestAccessDO();

        // 执行转换
        AppQuestionnaireAccessRespVO appRespVO = QuestionnaireAccessConvert.INSTANCE.convertToAppRespVO(accessDO);

        // 验证结果
        assertThat(appRespVO).isNotNull();
        assertThat(appRespVO.getAccessId()).isEqualTo(1001L);
        assertThat(appRespVO.getQuestionnaireId()).isEqualTo(1L);
        assertThat(appRespVO.getAccessTime()).isEqualTo(accessDO.getAccessTime());
        
        // 这些字段需要手动设置，转换器会忽略
        assertThat(appRespVO.getQuestionnaireTitle()).isNull();
        assertThat(appRespVO.getExternalLink()).isNull();
        assertThat(appRespVO.getAccessible()).isNull();
        assertThat(appRespVO.getStatusMessage()).isNull();
        assertThat(appRespVO.getEstimatedDuration()).isNull();
        assertThat(appRespVO.getQuestionCount()).isNull();
    }

    @Test
    void testConvertToAppRespVOList() {
        // 准备测试数据
        List<QuestionnaireAccessDO> accessDOList = Arrays.asList(
                createTestAccessDO(),
                createTestAccessDO()
        );

        // 执行转换
        List<AppQuestionnaireAccessRespVO> appRespVOList = QuestionnaireAccessConvert.INSTANCE.convertToAppRespVOList(accessDOList);

        // 验证结果
        assertThat(appRespVOList).isNotNull();
        assertThat(appRespVOList).hasSize(2);
        assertThat(appRespVOList.get(0).getAccessId()).isEqualTo(1001L);
        assertThat(appRespVOList.get(1).getAccessId()).isEqualTo(1001L);
    }

    @Test
    void testConvertToAppRespVOPage() {
        // 准备测试数据
        List<QuestionnaireAccessDO> accessDOList = Arrays.asList(
                createTestAccessDO(),
                createTestAccessDO()
        );
        PageResult<QuestionnaireAccessDO> pageResult = new PageResult<>(accessDOList, 2L);

        // 执行转换
        PageResult<AppQuestionnaireAccessRespVO> appRespVOPageResult = QuestionnaireAccessConvert.INSTANCE.convertToAppRespVOPage(pageResult);

        // 验证结果
        assertThat(appRespVOPageResult).isNotNull();
        assertThat(appRespVOPageResult.getList()).hasSize(2);
        assertThat(appRespVOPageResult.getTotal()).isEqualTo(2L);
        assertThat(appRespVOPageResult.getList().get(0).getAccessId()).isEqualTo(1001L);
    }

    // 辅助方法

    private QuestionnaireAccessDO createTestAccessDO() {
        QuestionnaireAccessDO accessDO = new QuestionnaireAccessDO();
        accessDO.setId(1001L);
        accessDO.setQuestionnaireId(1L);
        accessDO.setStudentProfileId(100L);
        accessDO.setAccessTime(LocalDateTime.now());
        accessDO.setClientIp("127.0.0.1");
        accessDO.setUserAgent("Mozilla/5.0");
        accessDO.setAccessSource(1);
        accessDO.setSessionDuration(300);
        accessDO.setCreateTime(LocalDateTime.now());
        return accessDO;
    }

}