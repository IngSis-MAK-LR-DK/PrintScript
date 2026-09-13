package edu.austral.ingsis.printscript.common.ast;

/** A node that produces a value: a literal, an identifier reference, or an operation. */
public sealed interface Expression extends ASTNode
        permits NumberLiteralExpression,
                StringLiteralExpression,
                BooleanLiteralExpression,
                IdentifierExpression,
                BinaryExpression {

    <R> R accept(ExpressionVisitor<R> visitor);
}
