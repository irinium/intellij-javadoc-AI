package com.github.intellijjavadocai;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

@Slf4j
public class GptApiErrorHandler implements ResponseErrorHandler {

  @Override
  public boolean hasError(ClientHttpResponse response) throws IOException {
    return response.getStatusCode().series() == Series.CLIENT_ERROR
        || response.getStatusCode().series() == Series.SERVER_ERROR;
  }

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {
    log.error(
        "HTTP Status Code: {}; Error: {}", response.getStatusCode(), response.getStatusText());
    if (response.getStatusCode().series() == Series.SERVER_ERROR
        && response.getStatusCode().value() != 429) {
      throw new IOException(
          "Server error: " + response.getStatusCode() + " " + response.getStatusText());
    }
  }
}
