package edu.austral.ingsis.printscript.common.ast;

/** A node that produces a value: a literal, an identifier reference, or a binary operation. */
public sealed interface Expression extends ASTNode
        permits NumberLiteralExpression,
                StringLiteralExpression,
                IdentifierExpression,
                BinaryExpression,
                ExtendedBinaryExpression {

    <R> R accept(ExpressionVisitor<R> visitor);
}
