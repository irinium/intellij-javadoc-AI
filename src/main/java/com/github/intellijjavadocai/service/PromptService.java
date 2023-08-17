package com.github.intellijjavadocai.service;

import com.github.intellijjavadocai.config.ApiConfig;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.*;

@Slf4j
@RequiredArgsConstructor
public class PromptService {
  private final ExecutorService executorService;

  public String sendPrompt(String prompt) throws JSONException {
    return executorService.sendPromptWithRetry(buildRequestEntity(prompt));
  }

  public String readPrompt(String template) {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(template);
    return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        .lines()
        .collect(Collectors.joining("\n"));
  }

  @NotNull
  private HttpEntity<String> buildRequestEntity(String prompt) throws JSONException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    headers.set("Authorization", "Bearer " + ApiConfig.getOpenaiApiKey());

    JSONObject bodyJson =
        new JSONObject()
            .put("prompt", prompt)
            .put("model", ApiConfig.getModelName())
            .put("max_tokens", Integer.parseInt(ApiConfig.getMaxTokens()))
            .put("temperature", Double.parseDouble(ApiConfig.getTemperature()));
    return new HttpEntity<>(bodyJson.toString(), headers);
  }
}
