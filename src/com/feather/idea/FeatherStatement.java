package com.feather.idea;

import java.util.Arrays;
import java.util.List;

public class FeatherStatement {

    private final List<String> deepProperties;
    private final List<String> methods;
    private final String property;
    private final int methodStart;
    private String text;

    FeatherStatement(String text) {
        this.text = text;
        String[] parts = text.split(":");
        String[] propertyParts = parts[0].split("\\.");
        this.property = propertyParts[0];
        this.methods = Arrays.asList(parts).subList(1, parts.length);
        this.deepProperties = Arrays.asList(propertyParts).subList(1, propertyParts.length);
        this.methodStart = parts[0].length() + 1;
    }

    public List<String> getDeepProperties() {
        return deepProperties;
    }

    List<String> getMethods() {
        return methods;
    }

    String getProperty() {
        return property;
    }

    int getMethodStart() {
        return methodStart;
    }

    int length() {
        return text.length();
    }
}
