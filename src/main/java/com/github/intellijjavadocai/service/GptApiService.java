package com.github.intellijjavadocai.service;

import com.github.intellijjavadocai.config.ApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.*;

@Slf4j
@RequiredArgsConstructor
public class GptApiService {
  private final ApiConfig apiConfig;
  private final GptExecutorService gptExecutorService;

  public String sendPrompt(String prompt) throws JSONException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    headers.set("Authorization", "Bearer " + apiConfig.getOpenaiApiKey());

    JSONObject bodyJson =
        new JSONObject().put("prompt", prompt).put("model", "text-davinci-003");
    HttpEntity<String> entity = new HttpEntity<>(bodyJson.toString(), headers);

    return gptExecutorService.sendPromptWithRetry(entity);
  }
}
