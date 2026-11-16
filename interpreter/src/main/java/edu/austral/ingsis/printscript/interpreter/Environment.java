package edu.austral.ingsis.printscript.interpreter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.austral.ingsis.printscript.common.SemanticException;
import edu.austral.ingsis.printscript.common.SourceRange;

/**
 * Holds the variables declared so far: each one's type, its value once it's assigned, and whether
 * it was declared {@code const}.
 *
 * <p>Immutable: {@link #declare} and {@link #assign} don't change this instance, they return a new
 * one with the extra binding.
 */
final class Environment {

    private final Map<String, String> declaredTypes;
    private final Map<String, Object> values;
    private final Set<String> constants;

    Environment() {
        this(Map.of(), Map.of(), Set.of());
    }

    private Environment(
            Map<String, String> declaredTypes, Map<String, Object> values, Set<String> constants) {
        this.declaredTypes = declaredTypes;
        this.values = values;
        this.constants = constants;
    }

    static boolean isKnownType(String type) {
        return type.equals("number") || type.equals("string") || type.equals("boolean");
    }

    Environment declare(
            String name, String type, boolean isConstant, SourceRange nameAt, SourceRange typeAt) {
        if (declaredTypes.containsKey(name)) {
            throw error("Variable '" + name + "' is already declared", nameAt);
        }
        if (!isKnownType(type)) {
            throw error("Unknown type '" + type + "'", typeAt);
        }
        Map<String, String> updatedTypes = new HashMap<>(declaredTypes);
        updatedTypes.put(name, type);
        Set<String> updatedConstants = constants;
        if (isConstant) {
            updatedConstants = new HashSet<>(constants);
            updatedConstants.add(name);
        }
        return new Environment(Map.copyOf(updatedTypes), values, Set.copyOf(updatedConstants));
    }

    Environment assign(String name, Object value, SourceRange nameAt, SourceRange valueAt) {
        String type = declaredTypes.get(name);
        if (type == null) {
            throw error("Variable '" + name + "' is not declared", nameAt);
        }
        if (constants.contains(name) && values.containsKey(name)) {
            throw error("Cannot reassign constant '" + name + "'", nameAt);
        }
        requireMatchingType(name, type, value, valueAt);
        Map<String, Object> updatedValues = new HashMap<>(values);
        updatedValues.put(name, value);
        return new Environment(declaredTypes, Map.copyOf(updatedValues), constants);
    }

    String typeOf(String name, SourceRange at) {
        String type = declaredTypes.get(name);
        if (type == null) {
            throw error("Variable '" + name + "' is not declared", at);
        }
        return type;
    }

    Object read(String name, SourceRange at) {
        if (!declaredTypes.containsKey(name)) {
            throw error("Variable '" + name + "' is not declared", at);
        }
        Object value = values.get(name);
        if (value == null) {
            throw error("Variable '" + name + "' is used before being assigned", at);
        }
        return value;
    }

    private void requireMatchingType(String name, String type, Object value, SourceRange at) {
        boolean matches =
                (type.equals("number") && value instanceof Double)
                        || (type.equals("string") && value instanceof String)
                        || (type.equals("boolean") && value instanceof Boolean);
        if (!matches) {
            throw error(
                    "Cannot assign a value of type '"
                            + runtimeTypeName(value)
                            + "' to variable '"
                            + name
                            + "' of type '"
                            + type
                            + "'",
                    at);
        }
    }

    private static SemanticException error(String message, SourceRange at) {
        return new SemanticException(message, at.start(), at.end());
    }

    private static String runtimeTypeName(Object value) {
        if (value instanceof Double) {
            return "number";
        }
        if (value instanceof Boolean) {
            return "boolean";
        }
        return "string";
    }
}
