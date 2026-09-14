package edu.austral.ingsis.printscript.interpreter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SemanticException;

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

    Environment declare(String name, String type, boolean isConstant, Position at) {
        if (declaredTypes.containsKey(name)) {
            throw new SemanticException("Variable '" + name + "' is already declared", at, at);
        }
        if (!type.equals("number") && !type.equals("string") && !type.equals("boolean")) {
            throw new SemanticException("Unknown type '" + type + "'", at, at);
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

    Environment assign(String name, Object value, Position at) {
        String type = declaredTypes.get(name);
        if (type == null) {
            throw new SemanticException("Variable '" + name + "' is not declared", at, at);
        }
        if (constants.contains(name) && values.containsKey(name)) {
            throw new SemanticException("Cannot reassign constant '" + name + "'", at, at);
        }
        requireMatchingType(name, type, value, at);
        Map<String, Object> updatedValues = new HashMap<>(values);
        updatedValues.put(name, value);
        return new Environment(declaredTypes, Map.copyOf(updatedValues), constants);
    }

    /**
     * Needed by {@code StatementExecutor} to know a variable's declared type before coercing a raw
     * {@code readInput}/{@code readEnv} string on assignment (not just on declaration, where the
     * type is already on hand from the statement itself).
     */
    String typeOf(String name, Position at) {
        String type = declaredTypes.get(name);
        if (type == null) {
            throw new SemanticException("Variable '" + name + "' is not declared", at, at);
        }
        return type;
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
                        || (type.equals("string") && value instanceof String)
                        || (type.equals("boolean") && value instanceof Boolean);
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
        if (value instanceof Double) {
            return "number";
        }
        if (value instanceof Boolean) {
            return "boolean";
        }
        return "string";
    }
}
