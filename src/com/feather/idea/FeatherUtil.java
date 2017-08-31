package com.feather.idea;

import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSClassIndex;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

class FeatherUtil {

    public static final StubIndexKey<String, TypeScriptClass> KEY = StubIndexKey.createIndexKey("typescript.class.index");

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
            } else {
                return findBequeathProperty(property, parent);
            }
        }
        return Optional.empty();
    }

    private static Optional<JSQualifiedNamedElement> findBequeathProperty(String property, PsiElement classElement) {
        PsiElement context = classElement.getContext();
        if (context != null) {
            Project project = classElement.getProject();
            GlobalSearchScope scope = GlobalSearchScope.allScope(project); // JSResolveUtil.getResolveScope(context);
            Collection<JSElement> elements = StubIndex.getElements(JSClassIndex.KEY, property, project, scope, JSElement.class);
            if (elements != null) {
                return elements.stream()
                        .flatMap(e -> findPropertyInClass(e, property))
                        .findFirst();
            }

        }
        return Optional.empty();
    }

    private static Stream<JSQualifiedNamedElement> findPropertyInClass(JSElement e, String property) {
        JSQualifiedNamedElement[] children = PsiTreeUtil.getChildrenOfType(e, JSQualifiedNamedElement.class);
        if (children != null) {
            Optional<JSQualifiedNamedElement> first = Stream.of(children)
                    .filter(f -> f.getName().equals(property) && isBequeathProperty(f))
                    .findFirst();
            return first.map(Stream::of).orElseGet(Stream::empty);
        }
        return Stream.empty();
    }

    private static boolean isBequeathProperty(JSQualifiedNamedElement element) {
        ES6FieldStatementImpl parent = PsiTreeUtil.getParentOfType(element, ES6FieldStatementImpl.class);
        return parent != null && PsiTreeUtil.findChildrenOfType(parent, JSProperty.class).stream().anyMatch(p -> "bequeath".equalsIgnoreCase(p.getName()));
    }
}
