package com.github.intellijjavadocai.service;

import com.github.intellijjavadocai.config.ApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutorService {
    private final RestTemplate restTemplate;
    private final ApiConfig apiConfig;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    String sendPromptWithRetry(HttpEntity<String> entity) throws JSONException {
        String result = "";
        int attempts = 0;

        while (attempts < apiConfig.getMaxRetries() && result.isEmpty()) {
            ResponseEntity<String> responseEntity = restTemplate.exchange(apiConfig.getOpenaiApiUrl(), HttpMethod.POST, entity, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                if(responseEntity.getBody() != null) {
                    JSONObject responseJson = new JSONObject(responseEntity.getBody());
                    result = responseJson.getJSONArray("choices")
                                    .getJSONObject(0).getString("text").trim();
                }else {
                    log.error("Response has an empty body: {};", responseEntity);
                }
            } else if (responseEntity.getStatusCode().is4xxClientError() &&
                       responseEntity.getStatusCode() != HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Error: {}; {}", responseEntity.getStatusCode(), responseEntity.getStatusCode().getReasonPhrase());
                break;
            }

            attempts = getAttempts(result, attempts, scheduledExecutorService);
        }

        scheduledExecutorService.shutdown();
        return result;
    }

    private int getAttempts(String result, int attempts, ScheduledExecutorService executorService) {
        if (result.isEmpty()) {
            long sleepTime = (long) (apiConfig.getWaitDuration() * Math.pow(2, attempts));
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
