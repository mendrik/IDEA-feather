package com.feather.idea;

import static com.feather.idea.Constants.doubleBraces;
import static com.feather.idea.Constants.singleBraces;
import static com.feather.idea.FeatherUtil.inTemplateMethod;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_METHOD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STATIC_FIELD;
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
        @NotNull AnnotationHolder holder, int braceLength) {
        highlight(isValid, start, fs, element, holder, braceLength, INSTANCE_FIELD);
    }

    void highlight(boolean isValid, int start, FeatherStatement fs, PsiElement element,
        @NotNull AnnotationHolder holder, int braceLength, TextAttributesKey style) {

        if (braceLength > 0) {
            addAnnotation(start - braceLength, start,
                BRACES, holder, true);
            addAnnotation(start + fs.length(), start + fs.length() + braceLength,
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
    }

    void doDoubleBraceMatches(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        doDoubleBraceMatches(element, holder, 2);
    }

    private void doDoubleBraceMatches(@NotNull final PsiElement element, @NotNull AnnotationHolder holder, int braceLength) {
        String text = element.getText();
        Matcher m = doubleBraces.matcher(text);
        int start = element.getTextRange().getStartOffset();
        while (m.find()) {
            FeatherStatement fs = new FeatherStatement(m.group(1));
            highlight(
                FeatherUtil.findField(fs.getProperty(), element).isPresent(),
                start + m.start(1),
                fs,
                element,
                holder,
                braceLength
            );
        }
    }

    void doSingleBraceMatches(PsiElement element, AnnotationHolder holder) {
        String text = element.getText();
        Matcher m = singleBraces.matcher(text);
        int start = element.getTextRange().getStartOffset();
        if (m.matches() && inTemplateMethod(element)) {
            String property = m.group(1);
            int matchStart = start + m.start(1);
            int matchEnd = start + m.end(1);
            addAnnotation(matchStart - 1, matchStart, BRACES, holder, true);
            addAnnotation(matchEnd, matchEnd + 1, BRACES, holder, true);

            TextRange tr = new TextRange(matchStart, matchEnd);
            Annotation annotation = holder.createInfoAnnotation(tr, null);
            if (FeatherUtil.findField(property, element).isPresent()) {
                annotation.setTextAttributes(INSTANCE_FIELD);
            } else {
                annotation.setTextAttributes(STATIC_FIELD);
            }
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
