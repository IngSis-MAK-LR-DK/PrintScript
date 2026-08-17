package edu.austral.ingsis.printscript.plugins.modulo;

import edu.austral.ingsis.printscript.common.OperatorDefinition;

/**
 * Adds the {@code %} operator to PrintScript. Registered via {@code META-INF/services} so the
 * core lexer/parser/interpreter discover it through {@link java.util.ServiceLoader} without ever
 * depending on this module at compile time.
 */
public final class ModuloOperator implements OperatorDefinition {

    @Override
    public String symbol() {
        return "%";
    }

    @Override
    public double apply(double left, double right) {
        return left % right;
    }
}
