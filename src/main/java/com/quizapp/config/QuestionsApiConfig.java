package com.quizapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "questions.api")
public class QuestionsApiConfig {

    private String baseUrl;

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public QuestionsApiConfig setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }
}