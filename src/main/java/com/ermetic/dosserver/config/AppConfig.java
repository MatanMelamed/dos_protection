package com.ermetic.dosserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    @ConfigurationProperties()
    public DosServerConfig dosServerConfig() {
        return new DosServerConfig();
    }
}
