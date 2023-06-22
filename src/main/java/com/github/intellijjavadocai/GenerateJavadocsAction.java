package com.github.intellijjavadocai;
import com.github.intellijjavadocai.generator.ChatGPTJavadocGenerator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class GenerateJavadocsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (project == null || psiFile == null) return;

        AnnotationConfigApplicationContext context = ApplicationContextProvider.getContext();
        ChatGPTJavadocGenerator generator = context.getBean(ChatGPTJavadocGenerator.class);

        psiFile.accept(new PsiRecursiveElementVisitor() {
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

    private void handlePsiMethod(PsiMethod method, ChatGPTJavadocGenerator generator, Project project) {
        if (method.getDocComment() != null) return;

        String javadoc = isTestMethod(method)
                         ? generator.generateJavadoc(method.getText(), true)
                         : generator.generateJavadoc(method.getText(), false);
        if (!javadoc.isEmpty()) {
            insertJavadocComment(project, method, javadoc);
        }
    }

    private void handlePsiClass(PsiClass psiClass, ChatGPTJavadocGenerator generator, Project project) {
        if (psiClass.getDocComment() != null) return;

        String javadoc = generator.generateJavadoc("Class " + psiClass.getName(), false);
        if (!javadoc.isEmpty()) {
            insertJavadocComment(project, psiClass, javadoc);
        }
    }

    private boolean isTestMethod(PsiMethod method) {
        PsiAnnotation junit4TestAnnotation = method.getModifierList().findAnnotation("org.junit.Test");
        PsiAnnotation junit5TestAnnotation = method.getModifierList().findAnnotation("org.junit.jupiter.api.Test");
        return junit4TestAnnotation != null || junit5TestAnnotation != null;
    }

    private void insertJavadocComment(Project project, PsiElement element, String javadocText) {
        Document document = FileDocumentManager.getInstance().getDocument(element.getContainingFile().getVirtualFile());
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