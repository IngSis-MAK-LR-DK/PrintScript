package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

public record NumberLiteralExpression(double value, Position start, Position end)
        implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        if (visitor instanceof NumberLiteralVisitor<R> v) {
            return v.visitNumberLiteral(this);
        }
        throw new UnsupportedOperationException(
                visitor.getClass().getName() + " does not support NumberLiteralExpression");
    }
}
