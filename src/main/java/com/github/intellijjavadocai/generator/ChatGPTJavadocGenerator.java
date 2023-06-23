package com.github.intellijjavadocai.generator;

import com.github.intellijjavadocai.service.GptApiService;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;

@Slf4j
@RequiredArgsConstructor
public class ChatGPTJavadocGenerator {

  public static final String METHOD_PROMPT_TEMPLATE = "method-prompt-template.txt";
  public static final String TEST_PROMPT_TEMPLATE = "test-prompt-template.txt";

  private final GptApiService gptApiService;

  public String generateJavadoc(String codeSnippet, String name, boolean isTest) {
    String promptTemplate =
        isTest
            ? readPromptTemplate(TEST_PROMPT_TEMPLATE)
            : readPromptTemplate(METHOD_PROMPT_TEMPLATE);

    String prompt = String.format(promptTemplate, name, codeSnippet);
    try {
      return gptApiService.sendPrompt(prompt);
    } catch (JSONException e) {
      log.error("Error while processing GPT-3 API response.", e);
      return "";
    }
  }

  private String readPromptTemplate(String template) {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(template);
    return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        .lines()
        .collect(Collectors.joining("\n"));
  }
}
