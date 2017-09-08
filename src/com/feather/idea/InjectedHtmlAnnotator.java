package com.feather.idea;

import static com.feather.idea.Constants.classSplitter;
import static com.feather.idea.FeatherUtil.inTemplateMethod;
import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;
import static com.intellij.psi.util.PsiTreeUtil.getContextOfType;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import java.util.regex.Matcher;
import org.jetbrains.annotations.NotNull;

public class InjectedHtmlAnnotator extends GenericAnnotator implements Annotator {


    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        JSStringTemplateExpression contextOfType = getContextOfType(element, JSStringTemplateExpression.class);
        if (contextOfType != null) {
            if (element instanceof XmlTag) {
                XmlTag tag = (XmlTag) element;
                tryTagAnnotation(tag.getName(), element, holder);
            } else if (element instanceof XmlAttribute) {
                doDoubleBraceMatches(element, holder);
                XmlAttribute attribute = (XmlAttribute) element;
                if ("class".equalsIgnoreCase(attribute.getName())) {
                    tryClassAnnotations(findChildOfType(element, XmlAttributeValue.class), holder);
                }
            } else if (element instanceof XmlAttributeValue) {
                doSingleBraceMatches(element, holder);
            } else if (element instanceof XmlText) {
                doDoubleBraceMatches(element, holder);
            }
        }
    }

    private void tryTagAnnotation(String name, PsiElement element, AnnotationHolder holder) {
        if (FeatherUtil.findClassBySelector(name, element).isPresent()) {
            FeatherStatement fs = new FeatherStatement(name);
            highlight(
                true,
                element.getTextRange().getStartOffset() + 1,
                fs,
                element,
                holder,
                0,
                DefaultLanguageHighlighterColors.CLASS_REFERENCE
            );
        }
    }

    private void tryClassAnnotations(XmlAttributeValue element, AnnotationHolder holder) {
        if (element == null) {
            return;
        }
        String text = element.getText();
        Matcher m = classSplitter.matcher(text);
        TextRange range = element.getTextRange();
        while (m.find()) {
            String selector = m.group(1);
            if (FeatherUtil.findClassBySelector("." + selector, element).isPresent()) {
                FeatherStatement fs = new FeatherStatement(selector);
                highlight(
                    true,
                    range.getStartOffset() + m.start(1),
                    fs,
                    element,
                    holder,
                    0,
                    DefaultLanguageHighlighterColors.CLASS_REFERENCE
                );
            }
        }
    }
}
