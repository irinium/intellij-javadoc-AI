package com.github.intellijjavadocai.service;

import com.github.intellijjavadocai.GptApiErrorHandler;
import com.github.intellijjavadocai.config.ApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GptApiService {

    private final ApiConfig apiConfig;
    private final ExecutorService executorService;

    public String sendPrompt(String prompt) throws JSONException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + apiConfig.getOpenaiApiKey());

        JSONObject bodyJson = new JSONObject().put("prompt", prompt).put("max_tokens", 50).put("temperature", 0.5);
        HttpEntity<String> entity = new HttpEntity<>(bodyJson.toString(), headers);

        return executorService.sendPromptWithRetry(entity);
    }
}
