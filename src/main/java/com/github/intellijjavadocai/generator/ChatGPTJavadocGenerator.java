package com.github.intellijjavadocai.generator;

import com.github.intellijjavadocai.service.GptApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatGPTJavadocGenerator {

    public static final String METHOD_PROMPT_TEMPLATE = "method-prompt-template.txt";
    public static final String TEST_PROMPT_TEMPLATE = "test-prompt-template.txt";

    private final GptApiService gptApiService;

    public String generateJavadoc(String codeSnippet, boolean isTest) {
        String templateName = isTest ? TEST_PROMPT_TEMPLATE : METHOD_PROMPT_TEMPLATE;
        String promptTemplate = readPromptTemplate(templateName);
        String prompt = String.format(promptTemplate, codeSnippet);
        try {
            return gptApiService.sendPrompt(prompt);
        }
        catch (JSONException e) {
            log.error("There was an error while sending the prompt");
        }
        return "";
    }

    private String readPromptTemplate(String template) {
        try {
            Path path = Paths.get(getClass().getClassLoader().getResource(template).getPath());
            return Files.readString(path);
        } catch (IOException e) {
            log.error("Error reading {}. Error: {}", template, e.getMessage(), e);
            return "";
        }
    }
}