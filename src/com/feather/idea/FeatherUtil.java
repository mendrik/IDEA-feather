package com.feather.idea;

import static com.intellij.psi.util.PsiTreeUtil.*;
import static java.util.Optional.ofNullable;

import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
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
import org.jetbrains.annotations.Nullable;

class FeatherUtil {

    static Optional<JSQualifiedNamedElement> findField(String property, PsiElement element) {
        TypeScriptClass parent = PsiTreeUtil.getContextOfType(element, TypeScriptClass.class);
        if (parent != null) {
            Optional<JSQualifiedNamedElement> field = findProperty(property, parent);
            if (field.isPresent()) {
                return field;
            }
            JSClass[] superClasses = parent.getSuperClasses();
            for (JSClass clazz : superClasses) {
                field = findProperty(property, clazz);
                if (field.isPresent()) {
                    return field;
                }
            }
            Optional<JSQualifiedNamedElement> transformerMethod = findTransformerMethod(property, parent);
            if (transformerMethod.isPresent()) {
                return transformerMethod;
            }
        }
        return Optional.empty();
    }

    private static Optional<JSQualifiedNamedElement> findProperty(String property, JSClass parent) {
        return PsiTreeUtil
            .<JSQualifiedNamedElement>findChildrenOfAnyType(parent,
                TypeScriptField.class,
                TypeScriptFunction.class
            )
            .stream()
            .filter(p -> Objects.equals(p.getName(), property))
            .findFirst();
    }

    private static Optional<JSQualifiedNamedElement> findTransformerMethod(String method, JSElement classElement) {
        Project project = classElement.getProject();
        GlobalSearchScope scope = JSResolveUtil.getResolveScope(classElement);
        return StubIndex.getElements(JSSymbolIndex2.KEY, method, project, scope, JSElement.class)
            .stream()
            .filter(e -> e instanceof JSQualifiedNamedElement)
            .map(e -> ((JSQualifiedNamedElement) e))
            .filter(x -> inDecoratedMethod("transformer", x))
            .findFirst();
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

    static boolean inDecoratedMethod(String decorator, PsiElement element) {
        if (element == null) {
            return false;
        }
        JSAttributeListOwner ctx = getContextOfType(element, JSAttributeListOwner.class);
        return ofNullable(ctx)
                .map(f -> inDecorateMethod(decorator, f))
                .orElse(false);
    }

    @Nullable
    private static boolean inDecorateMethod(String decorator, JSAttributeListOwner el) {
        ES6Decorator deco = PsiTreeUtil.findChildOfType(el, ES6Decorator.class);
        if (deco != null) {
            JSReferenceExpression decoCall = PsiTreeUtil.findChildOfType(deco, JSReferenceExpression.class);
            return (decoCall != null) && decorator.equalsIgnoreCase(decoCall.getReferenceName());
        }
        return false;
    }

}
