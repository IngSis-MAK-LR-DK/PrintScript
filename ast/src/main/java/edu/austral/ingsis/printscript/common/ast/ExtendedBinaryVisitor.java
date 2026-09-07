package edu.austral.ingsis.printscript.common.ast;

/**
 * Implemented by an {@link ExpressionVisitor} that knows how to handle an {@link
 * ExtendedBinaryExpression}.
 */
public interface ExtendedBinaryVisitor<R> extends ExpressionVisitor<R> {

    R visitExtendedBinary(ExtendedBinaryExpression expression);
}
