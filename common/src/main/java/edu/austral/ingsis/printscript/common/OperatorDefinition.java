package edu.austral.ingsis.printscript.common;

/**
 * A binary operator contributed by a plugin module, found at runtime through {@link
 * java.util.ServiceLoader}. The core lexer/parser/interpreter never depend on any implementation of
 * this interface at compile time.
 */
public interface OperatorDefinition {

    /** A single character, e.g. {@code "%"}. */
    String symbol();

    double apply(double left, double right);
}
