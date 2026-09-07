package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

public record IdentifierExpression(String name, Position start, Position end)
        implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        if (visitor instanceof IdentifierVisitor<R> v) {
            return v.visitIdentifier(this);
        }
        throw new UnsupportedOperationException(
                visitor.getClass().getName() + " does not support IdentifierExpression");
    }
}
