package com.feather.idea;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlText;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class FeatherAnnotator implements Annotator {


    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        JSStringTemplateExpression contextOfType = PsiTreeUtil.getContextOfType(element, JSStringTemplateExpression.class);
        if (contextOfType != null) {
            Pattern pattern = Pattern.compile("\\{\\{[^}{]+}}");
            if (element instanceof XmlAttribute) {
                XmlAttribute a = (XmlAttribute) element;
                String name = a.getName();
                if (name.startsWith("{{") && name.endsWith("}}")) {
                    TextRange range = element.getTextRange();
                    highlight(
                            range.getStartOffset(),
                            range.getEndOffset(),
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
                            holder
                    );
                }
                m.reset();
            }
        }
    }

    private void highlight(int start, int end, @NotNull AnnotationHolder holder) {
        TextRange bracketOpen = new TextRange(start, start + 2);
        Annotation annotation1 = holder.createInfoAnnotation(bracketOpen, null);
        annotation1.setTextAttributes(DefaultLanguageHighlighterColors.BRACKETS);

        TextRange range = new TextRange(start + 2, end - 2);
        Annotation annotation2 = holder.createInfoAnnotation(range, null);
        annotation2.setTextAttributes(DefaultLanguageHighlighterColors.INSTANCE_FIELD);

        TextRange bracketClose = new TextRange(end - 2, end);
        Annotation annotation3 = holder.createInfoAnnotation(bracketClose, null);
        annotation3.setTextAttributes(DefaultLanguageHighlighterColors.BRACKETS);
    }

}
