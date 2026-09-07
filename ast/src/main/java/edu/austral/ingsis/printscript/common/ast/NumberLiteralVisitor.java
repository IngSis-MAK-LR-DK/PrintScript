package edu.austral.ingsis.printscript.common.ast;

/**
 * Implemented by an {@link ExpressionVisitor} that knows how to handle a {@link
 * NumberLiteralExpression}.
 */
public interface NumberLiteralVisitor<R> extends ExpressionVisitor<R> {

    R visitNumberLiteral(NumberLiteralExpression expression);
}
