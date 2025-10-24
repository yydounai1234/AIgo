package com.aigo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应格式
 * 成功响应: { "success": true, "data": {...} }
 * 错误响应: { "success": false, "error": {...} }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    private Boolean success;
    private T data;
    private ErrorInfo error;
    
    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
    
    /**
     * 空数据成功响应
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setError(new ErrorInfo(code, message));
        return response;
    }
    
    /**
     * 错误响应（使用ErrorCode枚举）
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }
    
    /**
     * 错误响应（使用ErrorCode枚举，自定义消息）
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String customMessage) {
        return error(errorCode.getCode(), customMessage);
    }
}
