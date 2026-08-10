package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

public record StringLiteralExpression(String value, Position start, Position end) implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitStringLiteral(this);
    }
}
