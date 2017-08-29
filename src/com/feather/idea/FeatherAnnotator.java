package com.feather.idea;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlText;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class FeatherAnnotator extends GenericAnnotator implements Annotator {


    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        JSStringTemplateExpression contextOfType = PsiTreeUtil.getContextOfType(element, JSStringTemplateExpression.class);
        if (contextOfType != null) {
            Pattern pattern = Pattern.compile("\\{\\{([^}{]+)}}");
            if (element instanceof XmlAttribute) {
                XmlAttribute a = (XmlAttribute) element;
                String name = a.getName();
                if (name.startsWith("{{") && name.endsWith("}}")) {
                    TextRange range = element.getTextRange();
                    highlight(
                            range.getStartOffset(),
                            range.getEndOffset(),
                            element,
                            name.substring(2, name.length() - 2),
                            holder
                    );
                }
            } else if (element instanceof XmlAttributeValue) {
                XmlAttributeValue av = (XmlAttributeValue) element;
                String value = av.getValue();
                TextRange range = av.getValueTextRange();
                if (value.startsWith("{{") && value.endsWith("}}")) {
                    highlight(
                        range.getStartOffset(),
                        range.getEndOffset(),
                        element,
                        value.substring(2, value.length() - 2),
                        holder
                    );
                } else {
                    XmlAttribute a = PsiTreeUtil.getParentOfType(av, XmlAttribute.class);
                    if (a != null && a.getName().equalsIgnoreCase("class")) {
                        Matcher m = pattern.matcher(value);
                        while (m.find()) {
                            highlight(
                                    range.getStartOffset() + m.start(),
                                    range.getStartOffset() + m.end(),
                                    element,
                                    m.group(1),
                                    holder
                            );
                        }
                        m.reset();
                    }
                }
            } else if (element instanceof XmlText) {
                XmlText text = (XmlText) element;
                Matcher m = pattern.matcher(text.getText());
                TextRange range = text.getTextRange();
                while (m.find()) {
                    highlight(
                            range.getStartOffset() + m.start(),
                            range.getStartOffset() + m.end(),
                            element,
                            m.group(1),
                            holder
                    );
                }
                m.reset();
            }
        }
    }
}
