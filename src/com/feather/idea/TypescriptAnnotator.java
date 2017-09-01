package com.feather.idea;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class TypescriptAnnotator extends GenericAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof JSLiteralExpression) {
            ES6Decorator deco = PsiTreeUtil.getParentOfType(element, ES6Decorator.class);
            if (deco != null) {
                doDoubleBraceMatches(element, holder);

                JSProperty prop = PsiTreeUtil.getParentOfType(element, JSProperty.class);
                if (prop != null && "body".equals(prop.getName())) {
                    String text = element.getText();
                    FeatherStatement fs = new FeatherStatement(text.substring(1, text.length() - 1));
                    highlight(
                        FeatherUtil.findField(fs.getProperty(), element).isPresent(),
                        element.getTextRange().getStartOffset() + 1,
                        fs,
                        element,
                        holder,
                        0
                    );
                }
            }
        }
    }
}
