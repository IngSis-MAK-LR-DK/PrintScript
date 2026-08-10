package edu.austral.ingsis.printscript.common.ast;

/** Double-dispatch contract implemented by the interpreter, formatter and analyzer. */
public interface ExpressionVisitor<R> {

    R visitNumberLiteral(NumberLiteralExpression expression);

    R visitStringLiteral(StringLiteralExpression expression);

    R visitIdentifier(IdentifierExpression expression);

    R visitBinary(BinaryExpression expression);
}
