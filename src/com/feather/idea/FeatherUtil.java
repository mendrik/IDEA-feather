package com.feather.idea;

import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

class FeatherUtil {

    static Optional<JSQualifiedNamedElement> findField(String property, PsiElement element) {
        TypeScriptClass parent = PsiTreeUtil.getParentOfType(element, TypeScriptClass.class);
        if (parent != null) {
            Optional<JSQualifiedNamedElement> field = PsiTreeUtil
                    .<JSQualifiedNamedElement>findChildrenOfAnyType(parent,
                            TypeScriptField.class,
                            TypeScriptFunction.class
                    )
                    .stream()
                    .filter(p  -> p.getName().equals(property))
                    .findFirst();
            if (field.isPresent()) {
                return field;
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static Collection<TypeScriptField> findBequeathProperties(PsiElement element) {
        Project project = element.getProject();
        ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        return Collections.emptyList();
    }

    private boolean isBequeathProperty(TypeScriptField element) {
        ES6FieldStatementImpl parent = PsiTreeUtil.getParentOfType(element, ES6FieldStatementImpl.class);
        if (parent != null) {
            return PsiTreeUtil.findChildrenOfType(parent, JSProperty.class)
                    .stream()
                    .anyMatch(p -> p.getName().equalsIgnoreCase("bequeath"));
        }
        return false;
    }
}
