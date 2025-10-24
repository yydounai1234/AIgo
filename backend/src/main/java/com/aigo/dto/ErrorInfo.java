package com.aigo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 错误信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorInfo {
    private String code;
    private String message;
}
