package edu.austral.ingsis.printscript.common.ast;

/**
 * Implemented by an {@link ExpressionVisitor} that knows how to handle an {@link
 * IdentifierExpression}.
 */
public interface IdentifierVisitor<R> extends ExpressionVisitor<R> {

    R visitIdentifier(IdentifierExpression expression);
}
