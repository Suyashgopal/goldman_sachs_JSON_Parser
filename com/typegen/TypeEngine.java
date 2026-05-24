package com.typegen;

import java.util.*;


public class TypeEngine {
    private final Map<String, Map<String, FieldInfo>> schemas = new LinkedHashMap<>();
    private int anonymousCounter = 0;

    
    // Generate TypeScript declarations from parsed JSON.
     
    public String generate(Object data, String rootTypeName) {
        if (!(data instanceof List)) {
            throw new IllegalArgumentException("Root JSON must be an array of objects");
        }

        List<?> items = (List<?>) data;
        if (items.isEmpty()) {
            return "export interface " + rootTypeName + " {}\n";
        }

        // Analyze all objects and merge their schemas
        for (Object item : items) {
            if (item instanceof Map) {
                analyzeObject((Map<?, ?>) item, rootTypeName);
            }
        }

        // Generate TypeScript output
        StringBuilder output = new StringBuilder();
        Set<String> generated = new LinkedHashSet<>();
        generateInterface(rootTypeName, output, generated);

        return output.toString();
    }

    
     // Recursively analyze an object and track field types.
     
    @SuppressWarnings("unchecked")
    private void analyzeObject(Map<?, ?> obj, String typeName) {
        Map<String, FieldInfo> schema = schemas.computeIfAbsent(typeName, k -> new LinkedHashMap<>());

        for (Map.Entry<?, ?> entry : obj.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            FieldInfo fieldInfo = schema.computeIfAbsent(key, k -> new FieldInfo());
            fieldInfo.occurrences++;

            String inferredType = inferType(value, typeName + "_" + capitalize(key));
            fieldInfo.types.add(inferredType);
        }
    }

    
      //Infer TypeScript type from a JSON value.
     
    @SuppressWarnings("unchecked")
    private String inferType(Object value, String suggestedTypeName) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "string";
        } else if (value instanceof Boolean) {
            return "boolean";
        } else if (value instanceof Number) {
            return "number";
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                return "any[]";
            }
            
            //  array elements
            Set<String> elementTypes = new LinkedHashSet<>();
            for (Object item : list) {
                elementTypes.add(inferType(item, suggestedTypeName + "Item"));
            }
            
            String elementType = elementTypes.size() == 1 
                ? elementTypes.iterator().next() 
                : "(" + String.join(" | ", elementTypes) + ")";
            
            return elementType + "[]";
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            if (map.isEmpty()) {
                return "{}";
            }
            
            String nestedTypeName = suggestedTypeName.isEmpty() 
                ? "Type" + (++anonymousCounter) 
                : suggestedTypeName;
            
            analyzeObject(map, nestedTypeName);
            return nestedTypeName;
        }
        
        return "any";
    }

    // Generate TypeScript interface declaration.

    private void generateInterface(String typeName, StringBuilder output, Set<String> generated) {
        if (generated.contains(typeName)) {
            return;
        }
        generated.add(typeName);

        Map<String, FieldInfo> schema = schemas.get(typeName);
        if (schema == null || schema.isEmpty()) {
            output.append("export interface ").append(typeName).append(" {}\n\n");
            return;
        }

        int totalObjects = schema.values().stream()
            .mapToInt(f -> f.occurrences)
            .max()
            .orElse(1);

        List<String> sortedKeys = new ArrayList<>(schema.keySet());
        Collections.sort(sortedKeys);

        for (String key : sortedKeys) {
            FieldInfo fieldInfo = schema.get(key);
            for (String type : fieldInfo.types) {
                if (schemas.containsKey(type)) {
                    generateInterface(type, output, generated);
                }
            }
        }

        output.append("export interface ").append(typeName).append(" {\n");
        
        for (String key : sortedKeys) {
            FieldInfo fieldInfo = schema.get(key);
            
            boolean isOptional = fieldInfo.occurrences < totalObjects;
            
            // Merge multiple types into union
            Set<String> uniqueTypes = new LinkedHashSet<>(fieldInfo.types);
            String typeUnion = uniqueTypes.size() == 1 
                ? uniqueTypes.iterator().next()
                : String.join(" | ", uniqueTypes);
            
            output.append("  ").append(key);
            if (isOptional) output.append("?");
            output.append(": ").append(typeUnion).append(";\n");
        }
        
        output.append("}\n\n");
    }

    /**
     * Capitalize first letter of a string.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Tracks field information across multiple object instances.
     */
    private static class FieldInfo {
        int occurrences = 0;
        List<String> types = new ArrayList<>();
    }
}
