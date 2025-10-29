package com.aigo.dto.work;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateWorkRequest {
    
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 100, message = "标题长度必须在1-100字符之间")
    private String title;
    
    @NotBlank(message = "作品简介不能为空")
    @Size(min = 1, max = 500, message = "描述长度必须在1-500字符之间")
    private String description;
    
    private Boolean isPublic = false;
    
    @Size(max = 500, message = "封面图片URL不能超过500字符")
    private String coverImage;
    
    @Size(max = 20, message = "内容类型长度不能超过20字符")
    private String contentType = "anime";
}
