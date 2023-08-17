package com.github.intellijjavadocai.action;

import com.github.intellijjavadocai.generator.Generator;
import com.github.intellijjavadocai.service.ExecutorService;
import com.github.intellijjavadocai.service.PromptService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class Action extends AnAction {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null || isDumbMode(project)) {
      return;
    }

    log.info("Generate Javadocs Action for the project {} started", project.getName());

    Editor editor = e.getData(CommonDataKeys.EDITOR);
    if (editor == null) {
      return;
    }

    PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

    if (!(psiFile instanceof PsiJavaFile)) {
      return;
    }

    // Get services from the project
    ExecutorService executorService = project.getService(ExecutorService.class);
    PromptService promptService = new PromptService(executorService);
    Generator generator = new Generator(promptService);

    psiFile.accept(
        new PsiRecursiveElementVisitor() {
          @Override
          public void visitElement(@NotNull PsiElement element) {
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

  private void handlePsiMethod(PsiMethod method, Generator generator, Project project) {
    if (method.getDocComment() != null) return;

    String javadoc =
        isTestMethod(method)
            ? generator.generateJavaDoc(method.getText(), true)
            : generator.generateJavaDoc(method.getText(), false);
    if (!javadoc.isEmpty()) {
      insertJavadocComment(project, method, javadoc);
    }
  }

  private void handlePsiClass(PsiClass psiClass, Generator generator, Project project) {
    if (psiClass.getDocComment() != null) return;

    String javadoc = generator.generateJavaDoc("Class " + psiClass.getName(), false);
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

      CommandProcessor.getInstance()
          .executeCommand(
              project,
              () ->
                  ApplicationManager.getApplication()
                      .runWriteAction(
                          () -> {
                            if (element instanceof PsiMethod || element instanceof PsiClass) {
                              int startPosition =
                                  element instanceof PsiMethod
                                      ? ((PsiMethod) element)
                                          .getModifierList()
                                          .getTextRange()
                                          .getStartOffset()
                                      : ((PsiClass) element)
                                          .getModifierList()
                                          .getTextRange()
                                          .getStartOffset();
                              int lineNumber = document.getLineNumber(startPosition);
                              int lineStartPosition = document.getLineStartOffset(lineNumber);

                              // Modify starting block comment based on generated Javadoc
                              String openingJavadoc = javadocText.startsWith("/**") ? "" : "/**";
                              document.insertString(
                                  lineStartPosition, openingJavadoc + javadocText.trim() + "\n");
                              PsiDocumentManager.getInstance(project).commitDocument(document);
                            }
                          }),
              "Insert Javadoc",
              "Generate Javadocs with AI");
    }
  }

  @Override
  public void update(AnActionEvent e) {
    PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    e.getPresentation().setEnabled(psiFile instanceof PsiJavaFile);
  }
}
