package com.feather.idea;

import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSSymbolIndex2;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Objects;
import java.util.Optional;

class FeatherUtil {

    static Optional<JSQualifiedNamedElement> findField(String property, PsiElement element) {
        TypeScriptClass parent = PsiTreeUtil.getContextOfType(element, TypeScriptClass.class);
        if (parent != null) {
            Optional<JSQualifiedNamedElement> field = PsiTreeUtil
                    .<JSQualifiedNamedElement>findChildrenOfAnyType(parent,
                            TypeScriptField.class,
                            TypeScriptFunction.class
                    )
                    .stream()
                    .filter(p -> Objects.equals(p.getName(), property))
                    .findFirst();
            if (field.isPresent()) {
                return field;
            }
        }
        return findBequeathProperty(property, parent);
    }

    private static Optional<JSQualifiedNamedElement> findBequeathProperty(String property, JSElement classElement) {
        Project project = classElement.getProject();
        GlobalSearchScope scope = JSResolveUtil.getResolveScope(classElement);
        return StubIndex.getElements(JSSymbolIndex2.KEY, property, project, scope, JSElement.class)
                .stream()
                .filter(e -> e instanceof JSQualifiedNamedElement)
                .map(e -> ((JSQualifiedNamedElement) e))
                .filter(FeatherUtil::isBequeathProperty)
                .findFirst();
    }

    private static boolean isBequeathProperty(JSQualifiedNamedElement element) {
        ES6FieldStatementImpl parent = PsiTreeUtil.getParentOfType(element, ES6FieldStatementImpl.class);
        return parent != null && PsiTreeUtil.findChildrenOfType(parent, JSProperty.class).stream().anyMatch(p -> "bequeath".equalsIgnoreCase(p.getName()));
    }
}
