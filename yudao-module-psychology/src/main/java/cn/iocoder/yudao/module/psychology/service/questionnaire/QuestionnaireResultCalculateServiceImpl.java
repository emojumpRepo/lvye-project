package cn.iocoder.yudao.module.psychology.service.questionnaire;

import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileVO;
import cn.iocoder.yudao.module.psychology.controller.app.assessment.vo.WebAssessmentParticipateReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.questionnaire.QuestionnaireResultConfigDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.questionnaire.QuestionnaireResultConfigMapper;
import cn.iocoder.yudao.module.psychology.enums.QuestionnaireResultCalculateTypeEnum;
import cn.iocoder.yudao.module.psychology.service.questionnaire.vo.AgeAndSexAndScoreFormulaVO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.vo.MostChooseFormulaVO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.vo.QuestionnaireResultVO;
import cn.iocoder.yudao.module.psychology.service.questionnaire.vo.ScoreBetweenFormulaVO;
import cn.iocoder.yudao.module.psychology.util.NumberUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:问卷结果计算服务层
 * @Version: 1.0
 */
@Service
public class QuestionnaireResultCalculateServiceImpl implements QuestionnaireResultCalculateService{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String INDEX_ALL = "all";

    @Resource
    private QuestionnaireResultConfigMapper resultConfigMapper;

    @Resource
    private StudentProfileMapper studentProfileMapper;

    @Override
    public List<QuestionnaireResultVO> resultCalculate(Long questionnaireId, Long userId, List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList) {
        List<QuestionnaireResultVO> resultList = new ArrayList<>();
        //获取学生档案
        StudentProfileVO studentProfile = studentProfileMapper.selectInfoByUserId(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int age = NumberUtils.calculateAge(studentProfile.getBirthDate().format(formatter));
        int sex = studentProfile.getSex();
        //问题维度结果数组，用于判断是否已匹配
        Set<String> diseaseResultSet = new HashSet<>();
        //读取问卷结果参数表进行匹配
        List<QuestionnaireResultConfigDO> configList = resultConfigMapper.selectListByQuestionnaireId(questionnaireId);
        for (int i = 0; i < configList.size(); i++) {
            QuestionnaireResultConfigDO resultConfigDO = configList.get(i);
            //如果该维度名称已登记，则跳过
            if (diseaseResultSet.contains(resultConfigDO.getDimensionName())) {
                continue;
            }
            //计算分数
            int score = this.calculateScoreByResultConfig(resultConfigDO, answerList);
            //年龄性别与分数 计算公式
            if (resultConfigDO.getCalculateType().equals(QuestionnaireResultCalculateTypeEnum.AGE_SEX_SCORE.getType())) {
                List<AgeAndSexAndScoreFormulaVO> FormulaVOList = JSONArray.parseArray(resultConfigDO.getCalculateFormula(), AgeAndSexAndScoreFormulaVO.class);
                //匹配公式，命中则输出结果，不命中continue
                for (AgeAndSexAndScoreFormulaVO formula : FormulaVOList) {
                    //判断性别，年龄，分数
                    if (sex != formula.getSex()
                            || !NumberUtils.isBetween(age, formula.getMinAge(), formula.getMaxAge())
                            || !NumberUtils.isBetween(score, formula.getMinScore(), formula.getMaxScore())) {
                        continue;
                    }
                    //命中的返回匹配结果
                    QuestionnaireResultVO resultVO = new QuestionnaireResultVO();
                    resultVO.setDimensionName(resultConfigDO.getDimensionName());
                    resultVO.setScore(score);
                    resultVO.setTeacherComment(resultConfigDO.getTeacherComment());
                    resultVO.setStudentComment(resultConfigDO.getStudentComment());
                    resultVO.setIsAbnormal(resultConfigDO.getIsAbnormal());
                    resultVO.setLevel(resultConfigDO.getLevel());
                    resultVO.setDescription(resultConfigDO.getDescription());
                    resultList.add(resultVO);
                }
            }
            // 分数区间计算公式
            else if (resultConfigDO.getCalculateType().equals(QuestionnaireResultCalculateTypeEnum.SCORE.getType())) {
                ScoreBetweenFormulaVO formula = JSON.parseObject(resultConfigDO.getCalculateFormula(), ScoreBetweenFormulaVO.class);
                //判断最高分与最低分区间
                if (!NumberUtils.isBetween(score, formula.getMinScore(), formula.getMaxScore())) {
                    continue;
                }
                //命中的返回匹配结果
                diseaseResultSet.add(resultConfigDO.getDimensionName());
                QuestionnaireResultVO resultVO = new QuestionnaireResultVO();
                resultVO.setDimensionName(resultConfigDO.getDimensionName());
                resultVO.setScore(score);
                resultVO.setTeacherComment(resultConfigDO.getTeacherComment());
                resultVO.setStudentComment(resultConfigDO.getStudentComment());
                resultVO.setIsAbnormal(resultConfigDO.getIsAbnormal());
                resultVO.setLevel(resultConfigDO.getLevel());
                resultVO.setDescription(resultConfigDO.getDescription());
                resultList.add(resultVO);
            }
            // 最多选择计算公式
            else if (resultConfigDO.getCalculateType().equals(QuestionnaireResultCalculateTypeEnum.MOST_CHOOSE.getType())) {
                MostChooseFormulaVO formula = JSON.parseObject(resultConfigDO.getCalculateFormula(), MostChooseFormulaVO.class);
                int chooseCount = 0;
                //累计题目分数选择的次数
                for (WebAssessmentParticipateReqVO.AssessmentAnswerItem answerItem : answerList) {
                    if (answerItem.getScore() == formula.getQuestionScore()) {
                        chooseCount = chooseCount + 1;
                    }
                }
                //判断是否大等于最多选择次数
                if (chooseCount < formula.getChooseCount()) {
                    continue;
                }
                //命中的返回匹配结果
                QuestionnaireResultVO resultVO = new QuestionnaireResultVO();
                resultVO.setDimensionName(resultConfigDO.getDimensionName());
                resultVO.setScore(score);
                resultVO.setTeacherComment(resultConfigDO.getTeacherComment());
                resultVO.setStudentComment(resultConfigDO.getStudentComment());
                resultVO.setIsAbnormal(resultConfigDO.getIsAbnormal());
                resultVO.setLevel(resultConfigDO.getLevel());
                resultVO.setDescription(resultConfigDO.getDescription());
                resultList.add(resultVO);
            }
        }
        return resultList;
    }

    /**
     * 根据每个问卷结果参数计算分数
     *
     * @return
     */
    public int calculateScoreByResultConfig(QuestionnaireResultConfigDO resultConfigDO, List<WebAssessmentParticipateReqVO.AssessmentAnswerItem> answerList) {
        //根据题目索引数据组计算分数
        int score = 0;
        //如果索引是ALL，则直接把每题分数全部加起来，否则匹配index算分
        if (resultConfigDO.getQuestionIndex().equals(INDEX_ALL)) {
            for (WebAssessmentParticipateReqVO.AssessmentAnswerItem answerItem : answerList) {
                score = score + answerItem.getScore();
            }
        } else {
            //题目索引数组
            int[] questionIndex = Arrays.stream(resultConfigDO.getQuestionIndex().split(","))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            for (int index : questionIndex) {
                for (WebAssessmentParticipateReqVO.AssessmentAnswerItem answerItem : answerList) {
                    if (answerItem.getIndex().equals(index)) {
                        score = score + answerItem.getScore();
                    }
                }
            }
        }
        return score;
    }

}
