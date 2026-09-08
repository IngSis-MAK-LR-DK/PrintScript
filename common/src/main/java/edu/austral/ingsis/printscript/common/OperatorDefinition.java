package edu.austral.ingsis.printscript.common;

/**
 * A binary operator, includes plugins found at runtime through {@link java.util.ServiceLoader}. The
 * core lexer/parser/interpreter never depend on any implementation of this interface at compile
 * time. All operators are left-associative.
 */
public interface OperatorDefinition {

    /** A single character, e.g. {@code "%"}. */
    String symbol();

    /**
     * Where this operator binds relative to others — see {@link OperatorPrecedence}. Expressed only
     * in terms of already-known operators (e.g. the four core ones in {@link CoreOperators}), so a
     * plugin never has to pick a magic number that might silently collide with another plugin's.
     */
    OperatorPrecedence precedence();

    double apply(double left, double right);
}
