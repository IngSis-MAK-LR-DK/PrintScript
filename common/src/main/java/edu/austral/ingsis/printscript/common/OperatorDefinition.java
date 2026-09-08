package edu.austral.ingsis.printscript.common;

/**
 * A binary operator, includes plugins found at runtime through {@link java.util.ServiceLoader}. The
 * core lexer/parser/interpreter never depend on any implementation of this interface at compile
 * time.
 */
public interface OperatorDefinition {

    /** A single character, e.g. {@code "%"}. */
    String symbol();

    /**
     * How tightly this operator binds — higher runs first. The four core operators use 1 (addition,
     * subtraction) and 2 (multiplication, division); a plugin operator picks whichever of those its
     * symbol should behave like. All operators are left-associative.
     */
    int precedence();

    double apply(double left, double right);
}
