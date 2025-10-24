package com.aigo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimeSegment {
    
    private List<Character> characters;
    
    private List<Scene> scenes;
    
    private String plotSummary;
    
    private String genre;
    
    private String mood;
}
