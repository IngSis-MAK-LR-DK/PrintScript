package edu.austral.ingsis.printscript.plugins.modulo;

import edu.austral.ingsis.printscript.common.OperatorDefinition;

/**
 * Adds the {@code %} operator to PrintScript. Registered through {@code META-INF/services}, so the
 * core lexer/parser/interpreter find it via {@link java.util.ServiceLoader} without ever depending
 * on this module at compile time.
 */
public final class ModuloOperator implements OperatorDefinition {

    @Override
    public String symbol() {
        return "%";
    }

    @Override
    public int precedence() {
        return 2; // same precedence as * and /
    }

    @Override
    public double apply(double left, double right) {
        return left % right;
    }
}
