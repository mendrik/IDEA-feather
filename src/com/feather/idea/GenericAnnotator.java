package com.feather.idea;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public abstract class GenericAnnotator implements Annotator {

    protected void highlight(int start, int end, PsiElement element, String innerText, @NotNull AnnotationHolder holder) {
        Project project = element.getProject();
        System.out.println(innerText);

        TextRange bracketOpen = new TextRange(start, start + 2);
        Annotation annotation1 = holder.createInfoAnnotation(bracketOpen, null);
        annotation1.setTextAttributes(DefaultLanguageHighlighterColors.METADATA);

        TextRange range = new TextRange(start + 2, end - 2);
        Annotation annotation2 = holder.createInfoAnnotation(range, null);
        annotation2.setTextAttributes(DefaultLanguageHighlighterColors.INSTANCE_FIELD);

        TextRange bracketClose = new TextRange(end - 2, end);
        Annotation annotation3 = holder.createInfoAnnotation(bracketClose, null);
        annotation3.setTextAttributes(DefaultLanguageHighlighterColors.METADATA);

        /**
         * holder
         * .createErrorAnnotation(range, "Unresolved property")
         * .registerFix(new CreatePropertyQuickFix(key));
         */
    }
}
