package edu.austral.ingsis.printscript.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleBinaryOperator;

/**
 * The four arithmetic operators every PrintScript program can use without any plugin installed.
 * They're modeled as {@link OperatorDefinition}s, so the lexer and parser never treat them any
 * differently from an operator a plugin contributes — the core is just what's installed by default.
 */
public final class CoreOperators {

    public static final OperatorDefinition PLUS =
            arithmetic("+", OperatorPrecedence.root(), (a, b) -> a + b);
    public static final OperatorDefinition MINUS =
            arithmetic("-", OperatorPrecedence.root(), (a, b) -> a - b);
    public static final OperatorDefinition MULTIPLY =
            arithmetic("*", OperatorPrecedence.higherThan(PLUS, MINUS), (a, b) -> a * b);
    public static final OperatorDefinition DIVIDE =
            arithmetic("/", OperatorPrecedence.higherThan(PLUS, MINUS), (a, b) -> a / b);

    private CoreOperators() {}

    public static Set<OperatorDefinition> all() {
        return Set.of(PLUS, MINUS, MULTIPLY, DIVIDE);
    }

    /**
     * Builds the full symbol -> operator table: the four core operators, plus whatever a plugin
     * contributes. Throws if a plugin claims a symbol that's already taken.
     */
    public static Map<String, OperatorDefinition> indexBySymbol(
            Set<OperatorDefinition> pluginOperators) {
        Map<String, OperatorDefinition> bySymbol = new HashMap<>();
        for (OperatorDefinition operator : all()) {
            bySymbol.put(operator.symbol(), operator);
        }
        for (OperatorDefinition operator : pluginOperators) {
            OperatorDefinition previous = bySymbol.put(operator.symbol(), operator);
            if (previous != null) {
                throw new IllegalArgumentException(
                        "Two operators both define the symbol '"
                                + operator.symbol()
                                + "': "
                                + previous.getClass().getName()
                                + " and "
                                + operator.getClass().getName());
            }
        }
        return Map.copyOf(bySymbol);
    }

    private static OperatorDefinition arithmetic(
            String symbol, OperatorPrecedence precedence, DoubleBinaryOperator op) {
        return new OperatorDefinition() {
            @Override
            public String symbol() {
                return symbol;
            }

            @Override
            public OperatorPrecedence precedence() {
                return precedence;
            }

            @Override
            public double apply(double left, double right) {
                return op.applyAsDouble(left, right);
            }
        };
    }
}
