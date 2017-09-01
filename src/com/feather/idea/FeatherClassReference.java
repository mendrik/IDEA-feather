package com.feather.idea;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FeatherClassReference extends PsiReferenceBase<XmlTag> {

    private String className;

    FeatherClassReference(@NotNull XmlTag element, TextRange textRange, String className) {
        super(element, textRange);
        this.className = className;
        System.out.println(className);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return FeatherUtil.findClassBySelector(className, myElement).orElse(null);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[]{
            LookupElementBuilder.create(className)
        };
    }
}
