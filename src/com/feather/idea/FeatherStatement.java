package com.feather.idea;

import java.util.Arrays;
import java.util.List;

public class FeatherStatement {

    private final List<String> deepProperties;
    private final List<String> methods;
    private final String property;
    private final int methodStart;

    public FeatherStatement(String text) {
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

    public List<String> getMethods() {
        return methods;
    }

    public String getProperty() {
        return property;
    }

    public int getMethodStart() {
        return methodStart;
    }
}
