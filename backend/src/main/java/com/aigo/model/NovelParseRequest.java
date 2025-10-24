package com.aigo.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NovelParseRequest {
    
    @NotBlank(message = "文本内容不能为空")
    private String text;
    
    private String style;
    
    private String targetAudience;
}
