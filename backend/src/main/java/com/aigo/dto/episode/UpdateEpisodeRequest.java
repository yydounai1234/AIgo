package com.aigo.dto.episode;

import com.aigo.entity.Episode;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateEpisodeRequest {
    
    @Size(min = 1, max = 100, message = "标题长度必须在1-100字符之间")
    private String title;
    
    private String novelText;
    
    private List<Episode.SceneData> scenes;
    
    private Boolean isFree;
    
    private Integer coinPrice;
}
