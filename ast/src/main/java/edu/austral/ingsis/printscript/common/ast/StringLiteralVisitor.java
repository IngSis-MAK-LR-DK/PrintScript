package edu.austral.ingsis.printscript.common.ast;

/**
 * Implemented by an {@link ExpressionVisitor} that knows how to handle a {@link
 * StringLiteralExpression}.
 */
public interface StringLiteralVisitor<R> extends ExpressionVisitor<R> {

    R visitStringLiteral(StringLiteralExpression expression);
}
