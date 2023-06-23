package com.github.intellijjavadocai.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiConfig {

  private final Properties openaiProperties =
      new Properties() {
        {
          try (InputStream input =
              ApiConfig.class.getClassLoader().getResourceAsStream("openai/config.properties")) {
            load(input);
          } catch (IOException e) {
            log.error("Error while loading openai properties: {}", e.getMessage());
          }
        }
      };

  public String getOpenaiApiKey() {
    return openaiProperties.getProperty("openai.apiKey");
  }

  public String getOpenaiApiUrl() {
    return openaiProperties.getProperty("openai.apiUrl");
  }

  public String getMaxRetries() {
    return openaiProperties.getProperty("openai.maxRetries");
  }

  public String getWaitDuration() {
    return openaiProperties.getProperty("openai.waitDuration");
  }

  public String getMadelName() {
    return openaiProperties.getProperty("openai.modelName");
  }
}
