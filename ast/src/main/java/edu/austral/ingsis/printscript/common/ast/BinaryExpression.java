package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.Position;

/**
 * A binary operation. {@code operator} may be one of the four core operators or one contributed by
 * a plugin — see {@link OperatorDefinition} — there's no distinction at the AST level.
 */
public record BinaryExpression(
        Expression left,
        OperatorDefinition operator,
        Expression right,
        Position start,
        Position end)
        implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitBinary(this);
    }
}
