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
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ProcessingContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import org.jetbrains.annotations.NotNull;

public class FeatherReferenceContributor extends PsiReferenceContributor implements Constants {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(
                        @NotNull PsiElement element,
                        @NotNull ProcessingContext context) {
                if (element instanceof XmlText ||
                    element instanceof XmlAttribute ||
                    stringInDecorator(element)) {
                        Matcher m = pattern.matcher(element.getText());
                        List<FeatherFieldReference> res = new ArrayList<>();
                        while (m.find()) {
                            res.addAll(getReferences(element, m));
                        }
                        return res.toArray(new PsiReference[0]);
                }
                return PsiReference.EMPTY_ARRAY;
                }
            }
        );
    }

    private Collection<FeatherFieldReference> getReferences(PsiElement element, Matcher m) {
        FeatherStatement fs = new FeatherStatement(m.group(1));
        List<FeatherFieldReference> res = new ArrayList<>();
        int start = m.start(1);
        int methodStart = fs.getMethodStart();
        res.add(new FeatherFieldReference(element, new TextRange(start, start + fs.getProperty().length())));
        for (String method : fs.getMethods()) {
            res.add(new FeatherFieldReference(element, new TextRange(start + methodStart, start + methodStart + method.length())));
            methodStart += method.length() + 1;
        }
        return res;
    }

    private boolean stringInDecorator(PsiElement element) {
        if (element instanceof JSLiteralExpression) {
            ES6Decorator deco = PsiTreeUtil.getParentOfType(element, ES6Decorator.class);
            return deco != null;
        }
        return false;
    }
}