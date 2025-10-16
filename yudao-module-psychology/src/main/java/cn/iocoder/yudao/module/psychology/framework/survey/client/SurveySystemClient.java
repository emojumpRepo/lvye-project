package cn.iocoder.yudao.module.psychology.framework.survey.client;

import cn.iocoder.yudao.module.psychology.framework.config.SurveySystemProperties;
import cn.iocoder.yudao.module.psychology.framework.survey.vo.ExternalSurveyApiResponse;
import cn.iocoder.yudao.module.psychology.framework.survey.vo.ExternalSurveyRespVO;
import cn.iocoder.yudao.module.psychology.framework.survey.vo.ExternalSurveyUpdateReqVO;
import cn.iocoder.yudao.module.psychology.framework.survey.vo.ExternalSurveyUpdateRespVO;
import cn.iocoder.yudao.module.psychology.framework.survey.vo.ExternalSurveyStatusReqVO;
import cn.iocoder.yudao.module.psychology.framework.survey.vo.ExternalServiceResult;
import cn.iocoder.yudao.module.psychology.framework.survey.vo.ExternalSurveyQuestionRespVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import java.net.SocketTimeoutException;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 外部问卷系统客户端
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class SurveySystemClient {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private SurveySystemProperties surveySystemProperties;

    /**
     * 获取外部问卷系统的问卷列表
     *
     * @return 问卷列表
     */
    public List<ExternalSurveyRespVO> getSurveyList() {
        if (!surveySystemProperties.getEnabled()) {
            log.info("[getSurveyList] 外部问卷系统同步已禁用");
            return Collections.emptyList();
        }

        String url = surveySystemProperties.getSurveyListUrl();
        log.info("[getSurveyList] 开始请求外部问卷系统，URL: {}", url);

        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 如果配置了API密钥，添加到请求头
            if (StringUtils.hasText(surveySystemProperties.getSurveyAdminToken())) {
                headers.set("Authorization", "Bearer " + surveySystemProperties.getSurveyAdminToken());
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 发送请求
            ResponseEntity<ExternalSurveyApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ExternalSurveyApiResponse.class
            );

            // 处理响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ExternalSurveyApiResponse apiResponse = response.getBody();
                
                if (apiResponse.isSuccess()) {
                    List<ExternalSurveyRespVO> surveys = apiResponse.getData();
                    log.info("[getSurveyList] 成功获取外部问卷列表，数量: {}", surveys != null ? surveys.size() : 0);
                    return surveys != null ? surveys : Collections.emptyList();
                } else {
                    log.error("[getSurveyList] 外部问卷系统返回错误，code: {}, message: {}", 
                            apiResponse.getCode(), apiResponse.getMessage());
                    return Collections.emptyList();
                }
            } else {
                log.error("[getSurveyList] 外部问卷系统响应异常，状态码: {}", response.getStatusCode());
                return Collections.emptyList();
            }

        } catch (RestClientException e) {
            log.error("[getSurveyList] 请求外部问卷系统失败，URL: {}, 错误: {}", url, e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("[getSurveyList] 处理外部问卷系统响应时发生异常", e);
            return Collections.emptyList();
        }
    }

    /**
     * 带重试机制的获取问卷列表
     *
     * @return 问卷列表
     */
    public List<ExternalSurveyRespVO> getSurveyListWithRetry() {
        int retryCount = surveySystemProperties.getRetryCount();
        
        for (int i = 0; i <= retryCount; i++) {
            try {
                List<ExternalSurveyRespVO> result = getSurveyList();
                if (!result.isEmpty()) {
                    return result;
                }
                
                if (i < retryCount) {
                    log.warn("[getSurveyListWithRetry] 第{}次请求返回空结果，准备重试", i + 1);
                    Thread.sleep(1000 * (i + 1)); // 递增延迟
                }
            } catch (Exception e) {
                if (i < retryCount) {
                    log.warn("[getSurveyListWithRetry] 第{}次请求失败，准备重试，错误: {}", i + 1, e.getMessage());
                    try {
                        Thread.sleep(1000 * (i + 1)); // 递增延迟
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("[getSurveyListWithRetry] 重试{}次后仍然失败", retryCount, e);
                }
            }
        }
        
        return Collections.emptyList();
    }

    /**
     * 更新外部问卷系统的简单配置
     *
     * @param updateReqVO 更新请求参数
     * @return 是否更新成功
     */
    public boolean updateSurveySimpleConfig(ExternalSurveyUpdateReqVO updateReqVO) {
        if (!surveySystemProperties.getEnabled()) {
            log.info("[updateSurveySimpleConfig] 外部问卷系统同步已禁用");
            return false;
        }

        String url = surveySystemProperties.getUpdateConfigUrl();
        log.info("[updateSurveySimpleConfig] 开始更新外部问卷配置，URL: {}, 参数: {}", url, updateReqVO);

        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + surveySystemProperties.getSurveyAdminToken());

            // 如果配置了API密钥，添加到请求头
            if (StringUtils.hasText(surveySystemProperties.getApiKey())) {
                headers.set("Authorization", "Bearer " + surveySystemProperties.getApiKey());
            }

            HttpEntity<ExternalSurveyUpdateReqVO> entity = new HttpEntity<>(updateReqVO, headers);

            // 发送请求
            ResponseEntity<ExternalSurveyUpdateRespVO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ExternalSurveyUpdateRespVO.class
            );

            // 处理响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ExternalSurveyUpdateRespVO updateResponse = response.getBody();

                if (updateResponse.isSuccess()) {
                    log.info("[updateSurveySimpleConfig] 外部问卷配置更新成功，surveyId: {}", updateReqVO.getSurveyId());
                    return true;
                } else {
                    log.error("[updateSurveySimpleConfig] 外部问卷配置更新失败，code: {}, message: {}",
                            updateResponse.getCode(), updateResponse.getMessage());
                    return false;
                }
            } else {
                log.error("[updateSurveySimpleConfig] 外部问卷配置更新响应异常，状态码: {}", response.getStatusCode());
                return false;
            }

        } catch (RestClientException e) {
            log.error("[updateSurveySimpleConfig] 更新外部问卷配置失败，URL: {}, 错误: {}", url, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("[updateSurveySimpleConfig] 处理外部问卷配置更新响应时发生异常", e);
            return false;
        }
    }

    /**
     * 发布问卷
     *
     * @param surveyId 外部问卷ID
     * @return 操作结果
     */
    public ExternalServiceResult publishSurvey(String surveyId) {
        if (!surveySystemProperties.getEnabled()) {
            log.info("[publishSurvey] 外部问卷系统同步已禁用");
            return ExternalServiceResult.error("外部问卷系统同步已禁用");
        }

        String url = surveySystemProperties.getPublishSurveyUrl();
        log.info("[publishSurvey] 开始发布问卷，URL: {}, surveyId: {}", url, surveyId);

        ExternalSurveyStatusReqVO requestVO = ExternalSurveyStatusReqVO.publishRequest(surveyId);
        return executeStatusOperation(url, requestVO, "发布");
    }

    /**
     * 暂停问卷
     *
     * @param surveyId 外部问卷ID
     * @return 操作结果
     */
    public ExternalServiceResult pauseSurvey(String surveyId) {
        if (!surveySystemProperties.getEnabled()) {
            log.info("[pauseSurvey] 外部问卷系统同步已禁用");
            return ExternalServiceResult.error("外部问卷系统同步已禁用");
        }

        String url = surveySystemProperties.getPauseSurveyUrl();
        log.info("[pauseSurvey] 开始暂停问卷，URL: {}, surveyId: {}", url, surveyId);

        ExternalSurveyStatusReqVO requestVO = ExternalSurveyStatusReqVO.pauseRequest(surveyId);
        return executeStatusOperation(url, requestVO, "暂停");
    }

    /**
     * 执行状态操作（发布/暂停）
     *
     * @param url 请求URL
     * @param requestVO 请求参数
     * @param operationName 操作名称（用于日志）
     * @return 操作结果
     */
    private ExternalServiceResult executeStatusOperation(String url, ExternalSurveyStatusReqVO requestVO, String operationName) {
        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 如果配置了API密钥，添加到请求头
            if (StringUtils.hasText(surveySystemProperties.getSurveyAdminToken())) {
                headers.set("Authorization", "Bearer " + surveySystemProperties.getSurveyAdminToken());
            }

            HttpEntity<ExternalSurveyStatusReqVO> entity = new HttpEntity<>(requestVO, headers);

            // 发送请求
            ResponseEntity<ExternalSurveyUpdateRespVO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ExternalSurveyUpdateRespVO.class
            );

            // 处理响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ExternalSurveyUpdateRespVO updateResponse = response.getBody();

                if (updateResponse.isSuccess()) {
                    log.info("[executeStatusOperation] {}问卷成功，surveyId: {}", operationName, requestVO.getSurveyId());
                    return ExternalServiceResult.success();
                } else {
                    String errorMsg = String.format("%s问卷失败: %s", operationName, updateResponse.getMessage());
                    log.error("[executeStatusOperation] {}问卷失败，code: {}, message: {}",
                            operationName, updateResponse.getCode(), updateResponse.getMessage());
                    return ExternalServiceResult.error(updateResponse.getCode(), errorMsg);
                }
            } else {
                String errorMsg = String.format("%s问卷失败: 服务器响应异常", operationName);
                log.error("[executeStatusOperation] {}问卷响应异常，状态码: {}", operationName, response.getStatusCode());
                return ExternalServiceResult.error(response.getStatusCode().value(), errorMsg);
            }

        } catch (ResourceAccessException e) {
            // 网络连接异常或超时
            String errorMsg = String.format("%s问卷失败: 网络连接异常", operationName);
            if (e.getCause() instanceof SocketTimeoutException) {
                errorMsg = String.format("%s问卷失败: 请求超时，请稍后重试", operationName);
                log.error("[executeStatusOperation] {}问卷超时，URL: {}, 错误: {}", operationName, url, e.getMessage());
                return ExternalServiceResult.timeoutError();
            } else {
                log.error("[executeStatusOperation] {}问卷网络异常，URL: {}, 错误: {}", operationName, url, e.getMessage(), e);
                return ExternalServiceResult.networkError(e.getMessage());
            }
        } catch (RestClientException e) {
            // HTTP状态码异常
            String errorMsg = String.format("%s问卷失败: %s", operationName, e.getMessage());
            log.error("[executeStatusOperation] {}问卷失败，URL: {}, 错误: {}", operationName, url, e.getMessage(), e);

            // 检查是否是认证失败
            if (e.getMessage().contains("401") || e.getMessage().contains("Unauthorized")) {
                return ExternalServiceResult.authError();
            }

            return ExternalServiceResult.error(errorMsg);
        } catch (Exception e) {
            // 其他异常
            String errorMsg = String.format("%s问卷失败: 系统内部错误", operationName);
            log.error("[executeStatusOperation] {}问卷时发生异常，URL: {}", operationName, url, e);
            return ExternalServiceResult.error(errorMsg);
        }
    }

    /**
     * 获取问卷题目
     *
     * @param surveyId 外部问卷ID
     * @return 问卷题目
     */
    public ExternalSurveyQuestionRespVO getSurveyQuestion(String surveyId) {
        if (!surveySystemProperties.getEnabled()) {
            log.info("[getSurveyQuestion] 外部问卷系统同步已禁用");
            return null;
        }

        String baseUrl = surveySystemProperties.getGetQuestionUrl();
        log.info("[getSurveyQuestion] 获取问卷题目URL: {}", baseUrl);
        String url = baseUrl + (baseUrl.contains("?") ? "&" : "?") + "surveyId=" + surveyId;
        log.info("[getSurveyQuestion] 开始请求外部问卷题目，URL: {}, surveyId: {}", url, surveyId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (StringUtils.hasText(surveySystemProperties.getSurveyAdminToken())) {
                headers.set("Authorization", "Bearer " + surveySystemProperties.getSurveyAdminToken());
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ExternalSurveyQuestionRespVO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ExternalSurveyQuestionRespVO.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ExternalSurveyQuestionRespVO body = response.getBody();
                if (body.isSuccess()) {
                    log.info("[getSurveyQuestion] 成功获取题目，题目数: {}", body.getData() == null ? 0 : body.getData().size());
                    return body;
                }
                log.error("[getSurveyQuestion] 外部问卷系统返回错误，code: {}, message: {}", body.getCode(), body.getMessage());
                return body; // 返回以便上层决定处理
            }

            log.error("[getSurveyQuestion] 外部问卷系统响应异常，状态码: {}", response.getStatusCode());
            return null;
        } catch (RestClientException e) {
            log.error("[getSurveyQuestion] 请求外部问卷系统失败，URL: {}, 错误: {}", url, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("[getSurveyQuestion] 处理外部问卷系统响应时发生异常", e);
            return null;
        }
    }

}
