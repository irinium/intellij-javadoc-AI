package com.github.intellijjavadocai.service;

import com.github.intellijjavadocai.GptApiErrorHandler;
import com.github.intellijjavadocai.config.ApiConfig;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class ExecutorService {
  private final RestTemplate restTemplate;
  private final ScheduledExecutorService scheduledExecutorService;
  private final Project project;

  public ExecutorService(Project project) {
    this.restTemplate = new RestTemplate();
    this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
    restTemplate.setErrorHandler(new GptApiErrorHandler());
    this.project = project;
  }

  public void sendPromptWithRetryInBackground(HttpEntity<String> entity, Consumer<String> onSuccess, Consumer<String> onError) {
    Task.Backgroundable task = new Task.Backgroundable(project, "Sending API Request", false) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        try {
          String result = sendPromptWithRetry(entity);
          onSuccess.accept(result);
        } catch (Exception e) {
          onError.accept(e.getMessage());
        }
      }
    };

    ProgressManager.getInstance().run(task);
  }

  String sendPromptWithRetry(HttpEntity<String> entity) throws JSONException {
    String result = "";
    int attempts = 0;

    while (attempts < Integer.parseInt(ApiConfig.getMaxRetries()) && result.isEmpty()) {
      log.info("Attempting to send request to {} with {} attempts", ApiConfig.getOpenaiApiUrl(), attempts);
      ResponseEntity<String> responseEntity =
          restTemplate.exchange(ApiConfig.getOpenaiApiUrl(), HttpMethod.POST, entity, String.class);

      if (responseEntity.getStatusCode().is2xxSuccessful()) {
        if (responseEntity.getBody() != null) {
          JSONObject responseJson = new JSONObject(responseEntity.getBody());
          result = responseJson.getJSONArray("choices").getJSONObject(0).getString("text").trim();
        } else {
          log.error("Response has an empty body: {};", responseEntity);
        }
      } else if (responseEntity.getStatusCode().is4xxClientError()
          && responseEntity.getStatusCode() != HttpStatus.TOO_MANY_REQUESTS) {
        log.error(
            "Error: {}; {}",
            responseEntity.getStatusCode(),
            responseEntity.getStatusCode().getReasonPhrase());
        break;
      }

      attempts = getAttempts(result, attempts, scheduledExecutorService);
    }

    scheduledExecutorService.shutdown();
    return result;
  }

  private int getAttempts(String result, int attempts, ScheduledExecutorService executorService) {
    if (result.isEmpty()) {
      long sleepTime = (long) (Long.parseLong(ApiConfig.getWaitDuration()) * Math.pow(2, attempts));
      attempts++;
      try {
        final CountDownLatch latch = new CountDownLatch(1);
        executorService.schedule(latch::countDown, sleepTime, TimeUnit.MILLISECONDS);
        latch.await();
      } catch (InterruptedException e) {
        log.error("Error while waiting for retry", e);
        Thread.currentThread().interrupt();
      }
    }
    return attempts;
  }
}
