package com.feather.idea;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlText;
import org.jetbrains.annotations.NotNull;

public class FeatherAnnotator extends GenericAnnotator implements Annotator {


    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        JSStringTemplateExpression contextOfType = PsiTreeUtil.getContextOfType(element, JSStringTemplateExpression.class);
        if (contextOfType != null) {
            if (element instanceof XmlAttribute) {
                System.out.println(element.getText());
                doMatches(element, holder);
            } else if (element instanceof XmlText) {
                doMatches(element, holder);
            }
        }
    }
}
