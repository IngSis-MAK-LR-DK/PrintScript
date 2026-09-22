package edu.austral.ingsis.printscript.interpreter;

import edu.austral.ingsis.printscript.common.SemanticException;
import edu.austral.ingsis.printscript.common.ast.Expression;

/**
 * Parses a raw runtime string - the result of {@code readInput}/{@code readEnv} - into the value a
 * declared PrintScript type expects: {@code "number"} -> {@link Double}, {@code "boolean"} -> a
 * strict {@link Boolean} (only the literal text {@code "true"}/{@code "false"}, trimmed), anything
 * else passes through unchanged as a {@link String}.
 *
 * <p>Split out of {@link StatementExecutor}: parsing untrusted external text into a typed value is
 * a different concern from executing a statement against an {@link Environment}.
 */
final class RuntimeValueCoercer {

    private RuntimeValueCoercer() {}

    static Object coerce(String raw, String declaredType, Expression expression) {
        return switch (declaredType) {
            case "number" -> parseNumber(raw, expression);
            case "boolean" -> parseBoolean(raw, expression);
            default -> raw; // "string", or an already-invalid type Environment.declare will reject
        };
    }

    private static Object parseNumber(String raw, Expression expression) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            throw new SemanticException(
                    "Value read at runtime ('" + raw + "') is not a valid 'number'",
                    expression.start(),
                    expression.end());
        }
    }

    private static Object parseBoolean(String raw, Expression expression) {
        String trimmed = raw.trim();
        if (trimmed.equals("true")) {
            return Boolean.TRUE;
        }
        if (trimmed.equals("false")) {
            return Boolean.FALSE;
        }
        throw new SemanticException(
                "Value read at runtime ('" + raw + "') is not a valid 'boolean'",
                expression.start(),
                expression.end());
    }
}
