package com.aigo.dto.episode;

import com.aigo.entity.Episode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateEpisodeRequest {
    
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 100, message = "标题长度必须在1-100字符之间")
    private String title;
    
    @NotBlank(message = "小说文本不能为空")
    private String novelText;
    
    private List<Episode.SceneData> scenes;
    
    @NotNull(message = "必须指定是否免费")
    private Boolean isFree;
    
    private Integer coinPrice = 0;
}
