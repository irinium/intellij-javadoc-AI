package com.github.intellijjavadocai;

import com.github.intellijjavadocai.config.ApiConfig;
import com.github.intellijjavadocai.generator.ChatGPTJavadocGenerator;
import com.github.intellijjavadocai.service.GptApiService;
import com.github.intellijjavadocai.service.GptExecutorService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateJavadocsAction extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getProject();
    PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    if (project == null || psiFile == null) return;

    log.info("Generate Javadocs Action for the project {} started", project.getName());

    if (isDumbMode(project)) {
      return;
    }

    // Get services from the project
    ApiConfig apiConfig = project.getService(ApiConfig.class);
    GptExecutorService executorService = new GptExecutorService(apiConfig);
    GptApiService gptApiService = new GptApiService(apiConfig, executorService);
    ChatGPTJavadocGenerator generator = new ChatGPTJavadocGenerator(gptApiService);

    psiFile.accept(
        new PsiRecursiveElementVisitor() {
          @Override
          public void visitElement(PsiElement element) {
            super.visitElement(element);

            if (element instanceof PsiMethod) {
              handlePsiMethod((PsiMethod) element, generator, project);
            }

            if (element instanceof PsiClass) {
              handlePsiClass((PsiClass) element, generator, project);
            }
          }
        });
  }

  private static boolean isDumbMode(Project project) {
    // Check if the project is in 'dumb mode' (indexing in progress)
    if (DumbService.isDumb(project)) {
      JOptionPane.showMessageDialog(
          null,
          "Javadoc generation is currently unavailable (indexing in progress).",
          "Javadoc Generation",
          JOptionPane.INFORMATION_MESSAGE);
      return true;
    }
    return false;
  }

  private void handlePsiMethod(
      PsiMethod method, ChatGPTJavadocGenerator generator, Project project) {
    if (method.getDocComment() != null) return;

    String javadoc =
        isTestMethod(method)
            ? generator.generateJavadoc(method.getText(), method.getName(), true)
            : generator.generateJavadoc(method.getText(), method.getName(), false);
    if (!javadoc.isEmpty()) {
      insertJavadocComment(project, method, javadoc);
    }
  }

  private void handlePsiClass(
      PsiClass psiClass, ChatGPTJavadocGenerator generator, Project project) {
    if (psiClass.getDocComment() != null) return;

    String javadoc =
        generator.generateJavadoc("Class " + psiClass.getName(), psiClass.getName(), false);
    if (!javadoc.isEmpty()) {
      insertJavadocComment(project, psiClass, javadoc);
    }
  }

  private boolean isTestMethod(PsiMethod method) {
    PsiAnnotation junit4TestAnnotation = method.getModifierList().findAnnotation("org.junit.Test");
    PsiAnnotation junit5TestAnnotation =
        method.getModifierList().findAnnotation("org.junit.jupiter.api.Test");
    return junit4TestAnnotation != null || junit5TestAnnotation != null;
  }

  private void insertJavadocComment(Project project, PsiElement element, String javadocText) {
    Document document =
        FileDocumentManager.getInstance().getDocument(element.getContainingFile().getVirtualFile());
    if (document != null) {
      PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
      int offset = element.getTextOffset();
      document.insertString(offset, javadocText + "\n");
      PsiDocumentManager.getInstance(project).commitDocument(document);
    }
  }

  @Override
  public void update(AnActionEvent e) {
    PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    e.getPresentation().setEnabled(psiFile instanceof PsiJavaFile);
  }
}
