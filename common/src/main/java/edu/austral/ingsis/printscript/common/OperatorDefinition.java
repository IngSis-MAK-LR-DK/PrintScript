package edu.austral.ingsis.printscript.common;

/**
 * A binary operator contributed by a plugin module, discovered at runtime via {@link
 * java.util.ServiceLoader}. The core lexer/parser/interpreter never depend on any implementation
 * of this interface at compile time.
 */
public interface OperatorDefinition {

    /** Exactly one character, e.g. {@code "%"}. */
    String symbol();

    double apply(double left, double right);
}
