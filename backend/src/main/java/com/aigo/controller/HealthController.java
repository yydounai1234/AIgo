package com.aigo.controller;

import com.aigo.dto.ApiResponse;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<HealthInfo> health() {
        HealthInfo healthInfo = new HealthInfo();
        healthInfo.setStatus("UP");
        healthInfo.setTimestamp(LocalDateTime.now());
        healthInfo.setService("AIgo Backend");
        healthInfo.setVersion("1.0.0");
        return ApiResponse.success(healthInfo);
    }

    @GetMapping("/hello")
    public ApiResponse<HelloInfo> hello() {
        HelloInfo helloInfo = new HelloInfo();
        helloInfo.setMessage("Hello from AIgo Backend!");
        helloInfo.setDescription("Spring Boot + LangChain4j integration is ready");
        return ApiResponse.success(helloInfo);
    }
    
    @Data
    public static class HealthInfo {
        private String status;
        private LocalDateTime timestamp;
        private String service;
        private String version;
    }
    
    @Data
    public static class HelloInfo {
        private String message;
        private String description;
    }
}
