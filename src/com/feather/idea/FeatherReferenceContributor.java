package com.feather.idea;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
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
import org.jetbrains.annotations.NotNull;

public class FeatherReferenceContributor extends PsiReferenceContributor implements Constants {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(),
            new PsiReferenceProvider() {
                public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
                    if (element instanceof XmlAttribute || element instanceof XmlAttributeValue || element instanceof XmlToken) {
                        return getPsiReferences(element);
                    } else if (element instanceof XmlTag) {
                        XmlTag tag = (XmlTag) element;
                        PsiReference[] references = getTagReference(tag.getName(), tag)
                            .map(psiReference -> new PsiReference[]{
                                psiReference
                            })
                            .orElseGet(() -> new PsiReference[0]);
                        if (references.length > 0) {
                            System.out.println(tag.getName());
                        }
                        return references;
                    }
                    return new PsiReference[0];
                }
            }
        );
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(JSLiteralExpression.class),
            new PsiReferenceProvider() {
                public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
                    return inDecorator(element) ? getPsiReferences(element) : new PsiReference[0];
                }
            }
        );
    }

    private Optional<FeatherClassReference> getTagReference(String tagName, XmlTag tagElement) {
        return FeatherUtil.findClassBySelector(tagName, tagElement)
            .map(e -> new FeatherClassReference(tagElement, new TextRange(1, tagName.length() + 1), tagName));
    }

    @NotNull
    private PsiReference[] getPsiReferences(@NotNull PsiElement element) {
        Matcher m = pattern.matcher(element.getText());
        List<FeatherFieldReference> res = new ArrayList<>();
        while (m.find()) {
            res.addAll(getReferences(element, m));
        }
        return res.toArray(new PsiReference[0]);
    }

    private Collection<FeatherFieldReference> getReferences(PsiElement element, Matcher m) {
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
