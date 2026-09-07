package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

public record StringLiteralExpression(String value, Position start, Position end)
        implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        if (visitor instanceof StringLiteralVisitor<R> v) {
            return v.visitStringLiteral(this);
        }
        throw new UnsupportedOperationException(
                visitor.getClass().getName() + " does not support StringLiteralExpression");
    }
}
