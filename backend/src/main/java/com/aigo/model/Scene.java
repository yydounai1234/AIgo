package com.aigo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scene {
    
    private int sceneNumber;
    
    private String character;
    
    private String dialogue;
    
    private String visualDescription;
    
    private String atmosphere;
    
    private String action;
}
