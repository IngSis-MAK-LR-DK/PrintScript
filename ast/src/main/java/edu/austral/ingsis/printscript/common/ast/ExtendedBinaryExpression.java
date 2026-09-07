package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.Position;

/**
 * A binary expression using an operator contributed by a plugin (see {@link OperatorDefinition}).
 */
public record ExtendedBinaryExpression(
        Expression left,
        OperatorDefinition operator,
        Expression right,
        Position start,
        Position end)
        implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        if (visitor instanceof ExtendedBinaryVisitor<R> v) {
            return v.visitExtendedBinary(this);
        }
        throw new UnsupportedOperationException(
                visitor.getClass().getName() + " does not support ExtendedBinaryExpression");
    }
}
