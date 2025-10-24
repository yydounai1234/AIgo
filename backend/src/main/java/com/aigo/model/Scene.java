package com.aigo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scene {
    
    private int sceneNumber;
    
    private String visualDescription;
    
    private String atmosphere;
    
    private List<String> dialogues;
    
    private String action;
}
