package com.github.intellijjavadocai.generator;

import com.github.intellijjavadocai.service.PromptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

@Slf4j
@RequiredArgsConstructor
public class JavaDocsGenerator {

  public static final String METHOD_PROMPT_TEMPLATE = "method-prompt-template.txt";
  public static final String TEST_PROMPT_TEMPLATE = "test-prompt-template.txt";

  private final PromptService promptService;

  public String generateJavaDoc(String codeSnippet, boolean isTest) {
    String promptTemplate =
                    isTest
            ? promptService.readPrompt(TEST_PROMPT_TEMPLATE)
            : promptService.readPrompt(METHOD_PROMPT_TEMPLATE);

    String prompt = String.format(promptTemplate, codeSnippet);
    try {
      return promptService.sendPrompt(prompt);
    } catch (JSONException e) {
      log.error("Error while processing GPT-3 API response.", e);
      return StringUtils.EMPTY;
    }
  }
}
