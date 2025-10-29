package com.aigo.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadAvatarRequest {
    
    @NotBlank(message = "头像数据不能为空")
    private String avatarData;
}
