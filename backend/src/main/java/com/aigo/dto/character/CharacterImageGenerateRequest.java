package com.aigo.dto.character;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharacterImageGenerateRequest {
    
    @NotBlank(message = "角色描述不能为空")
    private String description;
}
