package cn.iocoder.yudao.module.psychology.framework.survey.vo;

import lombok.Data;

/**
 * 外部服务调用结果
 *
 * @author 芋道源码
 */
@Data
public class ExternalServiceResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误码
     */
    private Integer errorCode;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 响应数据
     */
    private Object data;

    /**
     * 创建成功结果
     */
    public static ExternalServiceResult success() {
        ExternalServiceResult result = new ExternalServiceResult();
        result.setSuccess(true);
        return result;
    }

    /**
     * 创建成功结果（带数据）
     */
    public static ExternalServiceResult success(Object data) {
        ExternalServiceResult result = new ExternalServiceResult();
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    /**
     * 创建失败结果
     */
    public static ExternalServiceResult error(Integer errorCode, String errorMessage) {
        ExternalServiceResult result = new ExternalServiceResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        return result;
    }

    /**
     * 创建失败结果（只有错误消息）
     */
    public static ExternalServiceResult error(String errorMessage) {
        return error(500, errorMessage);
    }

    /**
     * 创建网络异常结果
     */
    public static ExternalServiceResult networkError(String errorMessage) {
        return error(503, "网络连接异常: " + errorMessage);
    }

    /**
     * 创建超时异常结果
     */
    public static ExternalServiceResult timeoutError() {
        return error(504, "请求超时，请稍后重试");
    }

    /**
     * 创建认证失败结果
     */
    public static ExternalServiceResult authError() {
        return error(401, "认证失败，请检查API密钥配置");
    }

}
