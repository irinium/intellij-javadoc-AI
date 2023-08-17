package com.github.intellijjavadocai.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiConfig {

  private static final Properties openaiProperties =
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

  public static String getOpenaiApiKey() {
    return System.getenv("OPENAI_API_KEY");
  }

  public static String getOpenaiApiUrl() {
    return openaiProperties.getProperty("openai.apiUrl");
  }

  public static String getMaxRetries() {
    return openaiProperties.getProperty("openai.maxRetries");
  }

  public static String getWaitDuration() {
    return openaiProperties.getProperty("openai.waitDuration");
  }

  public static String getModelName() {
    return openaiProperties.getProperty("openai.modelName");
  }

  public static String getMaxTokens() {
    return openaiProperties.getProperty("openai.maxTokens");
  }

  public static String getTemperature() {
    return openaiProperties.getProperty("openai.temperature");
  }
}
