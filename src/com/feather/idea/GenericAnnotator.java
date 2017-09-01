package com.feather.idea;

import static com.feather.idea.Constants.pattern;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_METHOD;
import static com.intellij.openapi.editor.colors.CodeInsightColors.ERRORS_ATTRIBUTES;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import java.util.Optional;
import java.util.regex.Matcher;
import org.jetbrains.annotations.NotNull;

abstract class GenericAnnotator implements Annotator {

    void highlight(boolean isValid, int start, FeatherStatement fs, PsiElement element,
        @NotNull AnnotationHolder holder, boolean brackets) {
        highlight(isValid, start, fs, element, holder, brackets, INSTANCE_FIELD);
    }

    void highlight(boolean isValid, int start, FeatherStatement fs, PsiElement element,
        @NotNull AnnotationHolder holder, boolean brackets, TextAttributesKey style) {

        if (brackets) {
            addAnnotation(start - 2, start,
                BRACES, holder, true);
        }

        addAnnotation(start, start + fs.getProperty().length(), style, holder, isValid);

        int methodStart = start + fs.getMethodStart();
        for (String method : fs.getMethods()) {
            Optional<JSQualifiedNamedElement> resolvedMethods =
                FeatherUtil.findField(method, element);
            addAnnotation(methodStart, methodStart + method.length(),
                INSTANCE_METHOD, holder, resolvedMethods.isPresent());
            methodStart += method.length() + 1;
        }

        if (brackets) {
            addAnnotation(start + fs.length(), start + fs.length() + 2, BRACES, holder, true);
        }
    }

    void doMatches(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        doMatches(element, holder, true);
    }

    private void doMatches(@NotNull final PsiElement element, @NotNull AnnotationHolder holder, boolean brackets) {
        String text = element.getText();
        Matcher m = pattern.matcher(text);
        int start = element.getTextRange().getStartOffset();
        while (m.find()) {
            FeatherStatement fs = new FeatherStatement(m.group(1));
            highlight(
                FeatherUtil.findField(fs.getProperty(), element).isPresent(),
                start + m.start(1),
                fs,
                element,
                holder,
                brackets
            );
        }
    }

    private void addAnnotation(int start, int end, TextAttributesKey color,
        @NotNull AnnotationHolder holder, boolean isValid) {

        TextRange range = new TextRange(start, end);
        Annotation annotation;
        if (isValid) {
            annotation = holder.createInfoAnnotation(range, null);
            annotation.setTextAttributes(color);
        } else {
            annotation = holder.createErrorAnnotation(range, "Unresolved property");
            annotation.setTextAttributes(ERRORS_ATTRIBUTES);
        }
    }
}
