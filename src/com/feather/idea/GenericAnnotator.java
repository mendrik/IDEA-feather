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
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import java.util.Optional;
import java.util.regex.Matcher;
import org.jetbrains.annotations.NotNull;

public abstract class GenericAnnotator implements Annotator {

    protected void highlight(int start, String innerText, PsiElement element, @NotNull AnnotationHolder holder) {
        highlight(start, innerText, element, holder, true);
    }

    protected void highlight(int start, String innerText, PsiElement element, @NotNull AnnotationHolder holder, boolean brackets) {
        FeatherStatement fs = new FeatherStatement(innerText);

        if (brackets) {
            addAnnotation(start - 2, start,
                    BRACES, holder, true);
        }

        Optional<JSQualifiedNamedElement> field =
                FeatherUtil.findField(fs.getProperty(), element);
        addAnnotation(start, start + fs.getProperty().length(),
                INSTANCE_FIELD, holder, field.isPresent());

        int methodStart = start + fs.getMethodStart();
        for (String method : fs.getMethods()) {
            Optional<JSQualifiedNamedElement> resolvedMethods =
                    FeatherUtil.findField(method, element);
            addAnnotation(methodStart, methodStart + method.length(),
                    INSTANCE_METHOD, holder, resolvedMethods.isPresent());
            methodStart += method.length() + 1;
        }

        if (brackets) {
            addAnnotation(start + innerText.length(), start + innerText.length() + 2, BRACES, holder, true);
        }
    }

    protected void doMatches(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        doMatches(element, holder, true);
    }
    protected void doMatches(@NotNull final PsiElement element, @NotNull AnnotationHolder holder, boolean brackets) {
        String text = element.getText();
        Matcher m = pattern.matcher(text);
        TextRange range = element.getTextRange();
        while (m.find()) {
            highlight(
                    range.getStartOffset() + m.start(1),
                    m.group(1),
                    element,
                    holder,
                    brackets
            );
        }

    }

    private void addAnnotation(int start, int end, TextAttributesKey color,
            @NotNull AnnotationHolder holder, boolean isProperty) {
        TextRange range = new TextRange(start, end);
        Annotation annotation;
        if (isProperty) {
            annotation = holder.createInfoAnnotation(range, null);
            annotation.setTextAttributes(color);
        } else {
            annotation = holder.createErrorAnnotation(range, "Unresolved property");
            annotation.setTextAttributes(ERRORS_ATTRIBUTES);
        }

    }

}
