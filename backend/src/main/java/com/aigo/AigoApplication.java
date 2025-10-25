package com.aigo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AigoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AigoApplication.class, args);
    }
}
