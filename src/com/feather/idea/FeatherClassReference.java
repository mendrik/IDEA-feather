package com.feather.idea;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FeatherClassReference extends PsiReferenceBase<PsiElement> {

    private TypeScriptClass typeScriptClass;

    FeatherClassReference(@NotNull PsiElement element, @NotNull TextRange textRange, TypeScriptClass typeScriptClass) {
        super(element, textRange);
        this.typeScriptClass = typeScriptClass;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return typeScriptClass;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[]{
            LookupElementBuilder.create(typeScriptClass)
        };
    }
}
