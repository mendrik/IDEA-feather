package com.feather.idea;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.search.searches.ReferencesSearch;
import java.util.Collections;
import java.util.List;

public class FeatherUtil {

    // classes = JSReferenceExpressionImpl ie Checkbox, GestureWidget
    // @Bind() checked: boolean; = ES6FieldStatementImpl
    // checked: boolen = TypeScriptFieldImpl
    // ES6DecoratorImpl

    public static List<TypeScriptField> findProperty(String property, Project project) {
        // ProjectRootManager.getFileIndex()
        // ReferencesSearch.search();
        /*
        for (VirtualFile virtualFile : virtualFiles) {
            SimpleFile simpleFile = (SimpleFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (simpleFile != null) {
                SimpleProperty[] properties = PsiTreeUtil.getChildrenOfType(simpleFile, SimpleProperty.class);
                if (properties != null) {
                    for (SimpleProperty property : properties) {
                        if (key.equals(property.getKey())) {
                            if (result == null) {
                                result = new ArrayList<SimpleProperty>();
                            }
                            result.add(property);
                        }
                    }
                }
            }
        }
        return result != null ? result : Collections.emptyList();
        */
        return Collections.emptyList();
    }
}
