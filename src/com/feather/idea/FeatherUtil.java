package com.feather.idea;

import com.intellij.openapi.project.Project;

public class FeatherUtil {

    // classes = JSReferenceExpressionImpl ie Checkbox, GestureWidget
    // @Bind() checked: boolean; = ES6FieldStatementImpl
    // checked: boolen = TypeScriptFieldImpl
    // ES6DecoratorImpl

    public static void findProperty(String property, Project project) {
/**
 *    List<SimpleProperty> result = null;
     Collection<VirtualFile> virtualFiles =
     FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, SimpleFileType.INSTANCE,
     GlobalSearchScope.allScope(project));
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
     return result != null ? result : Collections.<SimpleProperty>emptyList();
 */
    }
}
