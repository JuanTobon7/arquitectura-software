package com.cinecapas.persistence.configs;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("com.cinecapas.persistence.repos")
@EntityScan("com.cinecapas.persistence.daos")
public class PersistenceConfig {
    @PostConstruct
    void init() {
        System.out.println("🔥 PERSISTENCE CONFIG LOADED");
    }
}