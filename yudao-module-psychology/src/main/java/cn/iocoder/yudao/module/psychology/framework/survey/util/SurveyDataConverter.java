package cn.iocoder.yudao.module.psychology.framework.survey.util;

import cn.iocoder.yudao.module.psychology.framework.survey.enums.ExternalSurveyStatusEnum;
import cn.iocoder.yudao.module.psychology.framework.survey.vo.ExternalSurveyRespVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 问卷数据转换工具类
 *
 * @author 芋道源码
 */
@Slf4j
public class SurveyDataConverter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * 获取问卷类型对应的数字
     *
     * @param surveyType 外部问卷类型
     * @return 本地问卷类型数字
     */
    public static Integer convertSurveyType(String surveyType) {
        if (!StringUtils.hasText(surveyType)) {
            return 4; // 默认为其他类型
        }
        
        switch (surveyType.toLowerCase()) {
            case "normal":
                return 4; // 其他
            case "exam":
                return 1; // 考试
            case "vote":
                return 2; // 投票
            case "registration":
                return 3; // 报名
            default:
                return 4; // 其他
        }
    }

    /**
     * 转换外部状态为本地状态
     * 优先检查暂停状态，如果有暂停状态则返回已下线状态
     *
     * @param externalSurvey 外部问卷数据
     * @return 本地状态值
     */
    public static Integer convertStatus(ExternalSurveyRespVO externalSurvey) {
        // 优先检查暂停状态
        if (externalSurvey.isPaused()) {
            log.debug("[convertStatus] 问卷处于暂停状态，返回已下线状态，surveyId: {}", externalSurvey.getSurveyMetaId());
            return ExternalSurveyStatusEnum.PAUSING.getLocalStatus();
        }

        // 如果没有暂停状态，则根据当前状态转换
        String currentStatus = externalSurvey.getCurrentStatus();
        return ExternalSurveyStatusEnum.getLocalStatus(currentStatus);
    }

    /**
     * 生成问卷链接
     *
     * @param externalSurvey 外部问卷数据
     * @param baseUrl 外部系统基础URL
     * @return 问卷链接
     */
    public static String generateSurveyLink(ExternalSurveyRespVO externalSurvey, String baseUrl) {
        if (!StringUtils.hasText(externalSurvey.getSurveyPath())) {
            return "";
        }
        
        // 移除baseUrl末尾的斜杠
        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        
        // 生成问卷链接，外部系统的问卷访问路径为 /render/{surveyPath}?t={timestamp}
        return cleanBaseUrl + "/render/" + externalSurvey.getSurveyPath() + "?t=" + System.currentTimeMillis();
    }

    /**
     * 解析时间，支持多种时间格式
     *
     * @param timeStr 时间字符串
     * @return LocalDateTime对象，解析失败返回null
     */
    public static LocalDateTime parseTime(String timeStr) {
        if (!StringUtils.hasText(timeStr)) {
            return null;
        }

        try {
            // 首先尝试解析 ISO 8601 格式 (如: 2025-07-18T03:27:05.000Z)
            if (timeStr.contains("T") && (timeStr.endsWith("Z") || timeStr.contains("+"))) {
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(timeStr, ISO_DATE_TIME_FORMATTER);
                return offsetDateTime.toLocalDateTime();
            }

            // 然后尝试解析标准格式 (如: 2035-07-15 17:11:41)
            return LocalDateTime.parse(timeStr, DATE_TIME_FORMATTER);

        } catch (DateTimeParseException e) {
            log.warn("[parseTime] 解析时间失败: {}, 错误: {}", timeStr, e.getMessage());

            // 尝试其他可能的格式
            try {
                // 尝试只有日期的格式
                if (timeStr.length() == 10) {
                    return LocalDateTime.parse(timeStr + " 23:59:59", DATE_TIME_FORMATTER);
                }

                // 尝试去掉毫秒的ISO格式
                if (timeStr.contains("T") && timeStr.endsWith("Z")) {
                    String cleanTimeStr = timeStr.replace("Z", "");
                    if (cleanTimeStr.contains(".")) {
                        cleanTimeStr = cleanTimeStr.substring(0, cleanTimeStr.indexOf("."));
                    }
                    return LocalDateTime.parse(cleanTimeStr);
                }

            } catch (DateTimeParseException e2) {
                log.error("[parseTime] 所有时间格式解析都失败: {}", timeStr, e2);
            }

            return null;
        }
    }

    /**
     * 生成问卷描述
     *
     * @param externalSurvey 外部问卷数据
     * @return 问卷描述
     */
    public static String generateDescription(ExternalSurveyRespVO externalSurvey) {
        StringBuilder description = new StringBuilder();
        
        // 使用备注作为描述
        if (StringUtils.hasText(externalSurvey.getRemark())) {
            description.append(externalSurvey.getRemark());
        }
        
        // 添加问卷类型信息
        if (StringUtils.hasText(externalSurvey.getSurveyType())) {
            if (description.length() > 0) {
                description.append(" | ");
            }
            description.append("类型: ").append(externalSurvey.getSurveyType());
        }
        
        // 添加答题时间信息
        if (StringUtils.hasText(externalSurvey.getAnswerBegTime()) && 
            StringUtils.hasText(externalSurvey.getAnswerEndTime())) {
            if (description.length() > 0) {
                description.append(" | ");
            }
            description.append("答题时间: ")
                      .append(externalSurvey.getAnswerBegTime())
                      .append("-")
                      .append(externalSurvey.getAnswerEndTime());
        }
        
        return description.length() > 0 ? description.toString() : "外部问卷系统同步";
    }

    /**
     * 估算问卷时长（分钟）
     * 由于外部系统没有提供时长信息，这里根据问卷类型给出估算值
     *
     * @param surveyType 问卷类型
     * @return 估算时长（分钟）
     */
    public static Integer estimateDuration(String surveyType) {
        if (!StringUtils.hasText(surveyType)) {
            return 10; // 默认10分钟
        }
        
        switch (surveyType.toLowerCase()) {
            case "exam":
                return 30; // 考试类型估算30分钟
            case "vote":
                return 5;  // 投票类型估算5分钟
            case "registration":
                return 15; // 报名类型估算15分钟
            case "normal":
            default:
                return 10; // 普通问卷估算10分钟
        }
    }

    /**
     * 判断问卷是否开放
     * 如果有暂停状态，则不开放；否则根据当前状态判断
     *
     * @param externalSurvey 外部问卷数据
     * @return 是否开放：0-否，1-是
     */
    public static Integer isOpen(ExternalSurveyRespVO externalSurvey) {
        // 如果处于暂停状态，则不开放
        if (externalSurvey.isPaused()) {
            return 0;
        }

        // 根据当前状态判断是否开放
        String currentStatus = externalSurvey.getCurrentStatus();
        return "published".equals(currentStatus) ? 1 : 0;
    }

    /**
     * 生成目标人群信息
     *
     * @param externalSurvey 外部问卷数据
     * @return 目标人群：1-学生，2-家长
     */
    public static Integer generateTargetAudience(ExternalSurveyRespVO externalSurvey) {
        // 由于外部系统没有明确的目标人群字段，根据问卷类型推断
        String surveyType = externalSurvey.getSurveyType();
        if (!StringUtils.hasText(surveyType)) {
            return 1; // 默认学生
        }
        
        switch (surveyType.toLowerCase()) {
            case "exam":
                return 1; // 学生
            case "vote":
                return 1; // 学生
            case "registration":
                return 1; // 学生
            case "normal":
            default:
                return 1; // 默认学生
        }
    }

    /**
     * 将LocalDateTime转换为外部系统需要的时间格式字符串
     *
     * @param dateTime LocalDateTime对象
     * @return 格式化的时间字符串，格式为 yyyy-MM-dd HH:mm:ss
     */
    public static String formatTimeForExternal(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        try {
            return dateTime.format(DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("[formatTimeForExternal] 格式化时间失败: {}", dateTime, e);
            return null;
        }
    }

}
