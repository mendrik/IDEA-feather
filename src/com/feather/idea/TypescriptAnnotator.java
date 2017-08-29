package com.feather.idea;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class TypescriptAnnotator extends GenericAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        // System.out.println(element.getClass().getName() + ": " + element.getText());
        if (element instanceof TypeScriptField) {
            System.out.println(element.getText());
        } else if (element instanceof JSLiteralExpression) {
            ES6Decorator deco = PsiTreeUtil.getParentOfType(element, ES6Decorator.class);
            if (deco != null) {
                Pattern pattern = Pattern.compile("\\{\\{([^}{]+)}}");
                Matcher m = pattern.matcher(element.getText());
                TextRange range = element.getTextRange();
                while (m.find()) {
                    highlight(
                    range.getStartOffset() + m.start(),
                        range.getStartOffset() + m.end(),
                        element,
                        element.getText(),
                        holder
                    );
                }
                m.reset();
            }
        }
    }
}
