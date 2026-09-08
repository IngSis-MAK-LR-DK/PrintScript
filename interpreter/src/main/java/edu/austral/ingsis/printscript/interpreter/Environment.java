package edu.austral.ingsis.printscript.interpreter;

import java.util.HashMap;
import java.util.Map;

import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SemanticException;

/**
 * Holds the variables declared so far: each one's type, and its value once it's assigned.
 *
 * <p>Immutable: {@link #declare} and {@link #assign} don't change this instance, they return a new
 * one with the extra binding.
 */
final class Environment {

    private final Map<String, String> declaredTypes;
    private final Map<String, Object> values;

    Environment() {
        this(Map.of(), Map.of());
    }

    private Environment(Map<String, String> declaredTypes, Map<String, Object> values) {
        this.declaredTypes = declaredTypes;
        this.values = values;
    }

    Environment declare(String name, String type, Position at) {
        if (declaredTypes.containsKey(name)) {
            throw new SemanticException("Variable '" + name + "' is already declared", at, at);
        }
        if (!type.equals("number") && !type.equals("string")) {
            throw new SemanticException("Unknown type '" + type + "'", at, at);
        }
        Map<String, String> updatedTypes = new HashMap<>(declaredTypes);
        updatedTypes.put(name, type);
        return new Environment(Map.copyOf(updatedTypes), values);
    }

    Environment assign(String name, Object value, Position at) {
        String type = declaredTypes.get(name);
        if (type == null) {
            throw new SemanticException("Variable '" + name + "' is not declared", at, at);
        }
        requireMatchingType(name, type, value, at);
        Map<String, Object> updatedValues = new HashMap<>(values);
        updatedValues.put(name, value);
        return new Environment(declaredTypes, Map.copyOf(updatedValues));
    }

    Object read(String name, Position at) {
        if (!declaredTypes.containsKey(name)) {
            throw new SemanticException("Variable '" + name + "' is not declared", at, at);
        }
        Object value = values.get(name);
        if (value == null) {
            throw new SemanticException(
                    "Variable '" + name + "' is used before being assigned", at, at);
        }
        return value;
    }

    private void requireMatchingType(String name, String type, Object value, Position at) {
        boolean matches =
                (type.equals("number") && value instanceof Double)
                        || (type.equals("string") && value instanceof String);
        if (!matches) {
            throw new SemanticException(
                    "Cannot assign a value of type '"
                            + runtimeTypeName(value)
                            + "' to variable '"
                            + name
                            + "' of type '"
                            + type
                            + "'",
                    at,
                    at);
        }
    }

    private static String runtimeTypeName(Object value) {
        return value instanceof Double ? "number" : "string";
    }
}
