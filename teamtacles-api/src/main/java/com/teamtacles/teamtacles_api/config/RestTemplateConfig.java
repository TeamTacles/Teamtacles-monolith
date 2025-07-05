package com.teamtacles.teamtacles_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Value("${task-service.url}")
    private String taskServiceBaseUrl;

    @Bean
    public RestTemplate taskServiceRestTemplate(RestTemplateBuilder builder) {
        return builder
                .rootUri(taskServiceBaseUrl)
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}