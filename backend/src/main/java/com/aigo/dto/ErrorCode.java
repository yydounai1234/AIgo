package com.aigo.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 错误代码枚举
 */
@Getter
public enum ErrorCode {
    
    // 认证相关错误
    UNAUTHORIZED("UNAUTHORIZED", "未授权，Token 无效或过期", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "无权限访问该资源", HttpStatus.FORBIDDEN),
    
    // 资源相关错误
    NOT_FOUND("NOT_FOUND", "资源不存在", HttpStatus.NOT_FOUND),
    
    // 业务逻辑错误
    VALIDATION_ERROR("VALIDATION_ERROR", "请求参数验证失败", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_COINS("INSUFFICIENT_COINS", "金币余额不足", HttpStatus.BAD_REQUEST),
    ALREADY_PURCHASED("ALREADY_PURCHASED", "已购买过该内容", HttpStatus.BAD_REQUEST),
    ALREADY_PUBLISHED("ALREADY_PUBLISHED", "内容已发布", HttpStatus.BAD_REQUEST),
    ALREADY_LIKED("ALREADY_LIKED", "已点赞过", HttpStatus.BAD_REQUEST),
    
    // 系统错误
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "请求频率超限", HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_ERROR("INTERNAL_ERROR", "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
