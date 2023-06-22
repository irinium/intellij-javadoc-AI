package com.github.intellijjavadocai.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ApiConfig {
    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    @Value("${openai.api.maxRetries}")
    private int maxRetries;
    @Value("${openai.api.waitDuration}")
    private Long waitDuration;
}