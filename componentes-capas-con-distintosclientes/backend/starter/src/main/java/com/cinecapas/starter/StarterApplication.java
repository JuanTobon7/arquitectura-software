package com.cinecapas.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.cinecapas")
public class StarterApplication {

	public static void main(String[] args) {
        SpringApplication.run(StarterApplication.class, args);
	}

}
