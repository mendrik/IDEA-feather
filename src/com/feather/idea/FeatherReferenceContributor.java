package com.feather.idea;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.util.ProcessingContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;

public class FeatherReferenceContributor extends PsiReferenceContributor implements Constants {

    private static final PsiReference[] emptyReferences = new PsiReference[0];

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(),
            new PsiReferenceProvider() {
                public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
                    if (element instanceof XmlAttribute || element instanceof XmlAttributeValue || element instanceof XmlToken) {
                        PsiReference[] references = getHtmlReferences(element);
                        if (element instanceof XmlAttributeValue) {
                            XmlAttribute attribute = (XmlAttribute) element.getParent();
                            if ("class".equalsIgnoreCase(attribute.getName())) {
                                return (PsiReference[]) ArrayUtils.addAll(references, getCssClassReferences(element));
                            } else {
                                return (PsiReference[]) ArrayUtils.addAll(references, getSingleBraceReferences(element));
                            }
                        }
                        return references;
                    } else if (element instanceof XmlTag) {
                        XmlTag tag = (XmlTag) element;
                        return getTagReference(tag.getName(), tag)
                            .map(psiReference -> new PsiReference[]{
                                psiReference
                            })
                            .orElse(emptyReferences);
                    }
                    return emptyReferences;
                }
            }
        );
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(JSLiteralExpression.class),
            new PsiReferenceProvider() {
                public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
                    return inDecorator(element) ? getHtmlReferences(element) : new PsiReference[0];
                }
            }
        );
    }

    private PsiReference[] getSingleBraceReferences(PsiElement value) {
        Matcher m = singleBraces.matcher(value.getText());
        List<FeatherFieldReference> res = new ArrayList<>();
        while (m.find()) {
            String property = m.group(1);
            FeatherUtil.findField(property, value).ifPresent(f -> {
                TextRange textRange = new TextRange(m.start(1), m.end(1));
                res.add(
                    new FeatherFieldReference(value, textRange, property)
                );
            });
        }
        return res.toArray(new PsiReference[res.size()]);
    }

    private PsiReference[] getCssClassReferences(PsiElement value) {
        Matcher m = classSplitter.matcher(value.getText());
        List<FeatherClassReference> res = new ArrayList<>();
        while (m.find()) {
            String selector = "." + m.group(1);
            Optional<TypeScriptClass> classBySelector = FeatherUtil.findClassBySelector(selector, value);
            classBySelector.ifPresent(typeScriptClass -> res.add(
                new FeatherClassReference(value, new TextRange(m.start(1), m.end(1)), typeScriptClass)
            ));
        }
        return res.toArray(new PsiReference[res.size()]);
    }

    private Optional<FeatherClassReference> getTagReference(String tagName, XmlTag tagElement) {
        return FeatherUtil.findClassBySelector(tagName, tagElement)
            .map(e -> new FeatherClassReference(tagElement, new TextRange(1, tagName.length() + 1), e));
    }

    @NotNull
    private PsiReference[] getHtmlReferences(@NotNull PsiElement element) {
        Matcher m = doubleBraces.matcher(element.getText());
        List<FeatherFieldReference> res = new ArrayList<>();
        while (m.find()) {
            res.addAll(getReferencesToFields(element, m));
        }
        return res.toArray(new PsiReference[res.size()]);
    }

    private Collection<FeatherFieldReference> getReferencesToFields(PsiElement element, Matcher m) {
        FeatherStatement fs = new FeatherStatement(m.group(1));
        List<FeatherFieldReference> res = new ArrayList<>();
        int start = m.start(1);
        int methodStart = fs.getMethodStart();
        res.add(new FeatherFieldReference(
            element,
            new TextRange(start, start + fs.getProperty().length()),
            fs.getProperty()
        ));
        for (String method : fs.getMethods()) {
            res.add(new FeatherFieldReference(
                element,
                new TextRange(start + methodStart, start + methodStart + method.length()),
                method
            ));
            methodStart += method.length() + 1;
        }
        return res;
    }

    private boolean inDecorator(PsiElement element) {
        ES6Decorator deco = PsiTreeUtil.getParentOfType(element, ES6Decorator.class);
        return deco != null;
    }
}
