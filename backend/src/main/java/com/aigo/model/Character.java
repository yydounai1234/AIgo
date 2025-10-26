package com.aigo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Character {
    
    private String name;
    
    private String description;
    
    private String appearance;
    
    private String personality;
    
    private String gender;
    
    private String bodyType;
    
    private String facialFeatures;
    
    private String clothingStyle;
    
    private String distinguishingFeatures;
}
