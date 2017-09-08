package com.feather.idea;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;
import static com.intellij.psi.util.PsiTreeUtil.findChildrenOfType;
import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static java.util.Optional.ofNullable;

import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl;
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
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
import org.jetbrains.annotations.NotNull;

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
            return findBequeathProperty(property, parent);
        }
        return Optional.empty();
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
        ES6FieldStatementImpl parent = getParentOfType(element, ES6FieldStatementImpl.class);
        return parent != null && findChildrenOfType(parent, JSProperty.class).stream().anyMatch(p -> "bequeath".equalsIgnoreCase(p.getName()));
    }

    static Optional<TypeScriptClass> findClassBySelector(@NotNull String selector, @NotNull PsiElement classElement) {
        Project project = classElement.getProject();
        GlobalSearchScope scope = JSResolveUtil.getResolveScope(classElement);
        return StubIndex.getElements(JSSymbolIndex2.KEY, "selector", project, scope, JSElement.class)
            .stream()
            .filter(e -> e instanceof JSProperty)
            .map(e -> ((JSProperty) e))
            .filter(p ->
                ofNullable(findChildOfType(p, JSLiteralExpression.class))
                    .filter(s -> {
                        String value = s.getText();
                        return value.substring(1, value.length() - 1)
                            .equalsIgnoreCase(selector);
                    }).isPresent()
            )
            .findFirst()
            .flatMap(p ->
                ofNullable(getParentOfType(p, ES6Decorator.class))
                    .filter(d -> {
                        Optional<JSReferenceExpression> expr = PsiTreeUtil
                            .findChildrenOfType(d, JSReferenceExpression.class)
                            .stream()
                            .findFirst();
                        return expr.filter(jsReferenceExpression ->
                            "construct".equalsIgnoreCase(jsReferenceExpression
                                .getReferenceNameElement()
                                .getText()
                            ))
                            .isPresent();
                    })
                    .flatMap(d -> ofNullable(getParentOfType(d, TypeScriptClass.class)))
            );
    }

    static boolean inTemplateMethod(PsiElement element) {
        TypeScriptFunction func = PsiTreeUtil.getParentOfType(element, TypeScriptFunction.class);
        if (func != null) {
            ES6Decorator deco = PsiTreeUtil.findChildOfType(func, ES6Decorator.class);
            if (deco != null) {
                JSReferenceExpression decoCall = PsiTreeUtil.findChildOfType(deco, JSReferenceExpression.class);
                if (decoCall != null) {
                    if ("template".equalsIgnoreCase(decoCall.getReferenceName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
