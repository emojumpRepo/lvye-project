package cn.iocoder.yudao.module.psychology.controller.app.assessment.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 自定义响应结构
 * 支持多种返回格式
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomResponse<T> implements Serializable {

    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 成功标识
     */
    private Boolean success;
    
    /**
     * 状态
     */
    private String status;

    /**
     * 格式1：标准格式
     * {
     *   "code": 200,
     *   "message": "回调接收成功"
     * }
     */
    public static <T> CustomResponse<T> success(Integer code, String message) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    /**
     * 格式2：带成功标识的格式
     * {
     *   "success": true,
     *   "message": "数据已接收"
     * }
     */
    public static <T> CustomResponse<T> success(Boolean success, String message) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setSuccess(success);
        response.setMessage(message);
        return response;
    }

    /**
     * 格式3：带状态的格式
     * {
     *   "status": "success",
     *   "message": "处理完成"
     * }
     */
    public static <T> CustomResponse<T> success(String status, String message) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(status);
        response.setMessage(message);
        return response;
    }

    /**
     * 带数据的成功响应
     */
    public static <T> CustomResponse<T> success(Integer code, String message, T data) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 带数据的成功响应（格式2）
     */
    public static <T> CustomResponse<T> success(Boolean success, String message, T data) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setSuccess(success);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 带数据的成功响应（格式3）
     */
    public static <T> CustomResponse<T> success(String status, String message, T data) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(status);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 错误响应
     */
    public static <T> CustomResponse<T> error(Integer code, String message) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    /**
     * 错误响应（格式2）
     */
    public static <T> CustomResponse<T> error(Boolean success, String message) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setSuccess(success);
        response.setMessage(message);
        return response;
    }

    /**
     * 错误响应（格式3）
     */
    public static <T> CustomResponse<T> error(String status, String message) {
        CustomResponse<T> response = new CustomResponse<>();
        response.setStatus(status);
        response.setMessage(message);
        return response;
    }
}
