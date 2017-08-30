package com.feather.idea;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FeatherFieldReference extends PsiReferenceBase<PsiElement> {

    private String property;

    public FeatherFieldReference(@NotNull PsiElement element, TextRange textRange, String property) {
        super(element, textRange);
        this.property = property;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return FeatherUtil.findField(property, myElement).orElse(null);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
