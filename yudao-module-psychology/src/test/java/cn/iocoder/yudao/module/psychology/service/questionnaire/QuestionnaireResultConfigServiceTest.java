package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigPageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.questionnaire.vo.resultconfig.QuestionnaireResultConfigSaveReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultConfigMapper;
import cn.iocoder.yudao.module.psychology.service.questionnaire.impl.QuestionnaireResultConfigServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link QuestionnaireResultConfigService} 的单元测试类
 *
 * @author MinGoo
 */
@Import(QuestionnaireResultConfigServiceImpl.class)
public class QuestionnaireResultConfigServiceTest extends BaseDbUnitTest {

    @Resource
    private QuestionnaireResultConfigService questionnaireResultConfigService;

    @Resource
    private QuestionnaireResultConfigMapper questionnaireResultConfigMapper;

    @Test
    public void testCreateQuestionnaireResultConfig_success() {
        // 准备参数
        QuestionnaireResultConfigSaveReqVO reqVO = randomPojo(QuestionnaireResultConfigSaveReqVO.class);

        // 调用
        Long questionnaireResultConfigId = questionnaireResultConfigService.createQuestionnaireResultConfig(reqVO);
        // 断言
        assertNotNull(questionnaireResultConfigId);
        // 校验记录的属性是否正确
        QuestionnaireResultConfigDO questionnaireResultConfig = questionnaireResultConfigMapper.selectById(questionnaireResultConfigId);
        assertPojoEquals(reqVO, questionnaireResultConfig);
    }

    @Test
    public void testUpdateQuestionnaireResultConfig_success() {
        // mock 数据
        QuestionnaireResultConfigDO dbQuestionnaireResultConfig = randomPojo(QuestionnaireResultConfigDO.class);
        questionnaireResultConfigMapper.insert(dbQuestionnaireResultConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        QuestionnaireResultConfigSaveReqVO reqVO = randomPojo(QuestionnaireResultConfigSaveReqVO.class, o -> {
            o.setId(dbQuestionnaireResultConfig.getId()); // 设置更新的 ID
        });

        // 调用
        questionnaireResultConfigService.updateQuestionnaireResultConfig(reqVO);
        // 校验是否更新正确
        QuestionnaireResultConfigDO questionnaireResultConfig = questionnaireResultConfigMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, questionnaireResultConfig);
    }

    @Test
    public void testDeleteQuestionnaireResultConfig_success() {
        // mock 数据
        QuestionnaireResultConfigDO dbQuestionnaireResultConfig = randomPojo(QuestionnaireResultConfigDO.class);
        questionnaireResultConfigMapper.insert(dbQuestionnaireResultConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbQuestionnaireResultConfig.getId();

        // 调用
        questionnaireResultConfigService.deleteQuestionnaireResultConfig(id);
        // 校验数据不存在了
        assertNull(questionnaireResultConfigMapper.selectById(id));
    }

    @Test
    public void testGetQuestionnaireResultConfigPage() {
        // mock 数据
        QuestionnaireResultConfigDO dbQuestionnaireResultConfig = randomPojo(QuestionnaireResultConfigDO.class, o -> { // 等会查询到
            o.setQuestionnaireId(1L);
            o.setDimensionName("睡眠质量");
            o.setCalculateType(1);
            o.setIsAbnormal(0);
        });
        questionnaireResultConfigMapper.insert(dbQuestionnaireResultConfig);
        // 测试 questionnaireId 不匹配
        questionnaireResultConfigMapper.insert(cloneIgnoreId(dbQuestionnaireResultConfig, o -> o.setQuestionnaireId(2L)));
        // 测试 dimensionName 不匹配
        questionnaireResultConfigMapper.insert(cloneIgnoreId(dbQuestionnaireResultConfig, o -> o.setDimensionName("焦虑程度")));
        // 测试 calculateType 不匹配
        questionnaireResultConfigMapper.insert(cloneIgnoreId(dbQuestionnaireResultConfig, o -> o.setCalculateType(2)));
        // 测试 isAbnormal 不匹配
        questionnaireResultConfigMapper.insert(cloneIgnoreId(dbQuestionnaireResultConfig, o -> o.setIsAbnormal(1)));
        // 准备参数
        QuestionnaireResultConfigPageReqVO reqVO = new QuestionnaireResultConfigPageReqVO();
        reqVO.setQuestionnaireId(1L);
        reqVO.setDimensionName("睡眠");
        reqVO.setCalculateType(1);
        reqVO.setIsAbnormal(0);

        // 调用
        PageResult<QuestionnaireResultConfigDO> pageResult = questionnaireResultConfigService.getQuestionnaireResultConfigPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbQuestionnaireResultConfig, pageResult.getList().get(0));
    }

}
