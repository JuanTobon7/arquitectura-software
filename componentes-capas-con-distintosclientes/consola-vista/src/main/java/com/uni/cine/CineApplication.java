package com.uni.cine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.cinecapas")
public class CineApplication {
    public static void main(String[] args) {
        SpringApplication.run(CineApplication.class, args);
    }
}
