package edu.austral.ingsis.printscript.common.ast;

/**
 * Implemented by an {@link ExpressionVisitor} that knows how to handle a {@link BinaryExpression}.
 */
public interface BinaryVisitor<R> extends ExpressionVisitor<R> {

    R visitBinary(BinaryExpression expression);
}
