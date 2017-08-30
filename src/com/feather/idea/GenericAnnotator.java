package com.feather.idea;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_METHOD;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public abstract class GenericAnnotator implements Annotator {

    protected Pattern pattern = Pattern.compile("\\{\\{([^{}]+?)}}");

    protected void highlight(int start, String innerText, PsiElement element, @NotNull AnnotationHolder holder) {
        Project project = element.getProject();

        String[] parts = innerText.split(":");
        String[] propertyParts = parts[0].split("\\.");
        String property = propertyParts[0];
        List<String> deepProperties = Arrays.asList(propertyParts).subList(1, propertyParts.length);
        List<String> methods = Arrays.asList(parts).subList(1, parts.length);

        addAnnotation(start - 2, start, BRACES, holder);
        addAnnotation(start, start + property.length(), INSTANCE_FIELD, holder);
        int methodStart = start + parts[0].length() + 1;
        for (String method : methods) {
            addAnnotation(methodStart, methodStart + method.length(), INSTANCE_METHOD, holder);
            methodStart += method.length() + 1;
        }
        addAnnotation(start + innerText.length(), start + innerText.length() + 2, BRACES, holder);

        /**
         * holder
         * .createErrorAnnotation(range, "Unresolved property")
         * .registerFix(new CreatePropertyQuickFix(key));
         */
    }

    protected void doMatches(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        String text = element.getText();
        Matcher m = pattern.matcher(text);
        TextRange range = element.getTextRange();
        while (m.find()) {
            highlight(
                    range.getStartOffset() + m.start(1),
                    m.group(1),
                    element,
                    holder
            );
        }

    }

    private void addAnnotation(int start, int end, TextAttributesKey color, @NotNull AnnotationHolder holder) {
        TextRange range = new TextRange(start, end);
        Annotation annotation = holder.createInfoAnnotation(range, null);
        annotation.setTextAttributes(color);
    }

}
