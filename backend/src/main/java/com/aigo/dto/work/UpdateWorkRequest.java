package com.aigo.dto.work;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateWorkRequest {
    
    @Size(min = 1, max = 100, message = "标题长度必须在1-100字符之间")
    private String title;
    
    @Size(min = 1, max = 500, message = "描述长度必须在1-500字符之间")
    private String description;
    
    private Boolean isPublic;
    
    @Size(max = 500, message = "封面图片URL不能超过500字符")
    private String coverImage;
}
