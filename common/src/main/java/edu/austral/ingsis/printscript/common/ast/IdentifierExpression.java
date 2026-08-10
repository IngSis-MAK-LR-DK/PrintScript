package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

public record IdentifierExpression(String name, Position start, Position end) implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitIdentifier(this);
    }
}
