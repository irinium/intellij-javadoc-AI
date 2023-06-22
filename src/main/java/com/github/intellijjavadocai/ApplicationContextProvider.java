package com.github.intellijjavadocai;

import com.github.intellijjavadocai.config.ApiConfig;
import com.github.intellijjavadocai.generator.ChatGPTJavadocGenerator;
import com.github.intellijjavadocai.service.GptApiService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationContextProvider {
    private static final AnnotationConfigApplicationContext context;

    static {
        context = new AnnotationConfigApplicationContext(ApiConfig.class, ChatGPTJavadocGenerator.class, GptApiService.class);
    }

    public static AnnotationConfigApplicationContext getContext() {
        return context;
    }
}
