package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

public record BinaryExpression(
        Expression left, BinaryOperator operator, Expression right, Position start, Position end)
        implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        if (visitor instanceof BinaryVisitor<R> v) {
            return v.visitBinary(this);
        }
        throw new UnsupportedOperationException(
                visitor.getClass().getName() + " does not support BinaryExpression");
    }
}
