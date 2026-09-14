package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

/**
 * {@code readEnv(<variableName>)}. Same evaluation/coercion contract as {@link ReadInputExpression}
 * - always a raw {@code String}, coerced by the interpreter's statement executor only when directly
 * assigned/declared.
 */
public record ReadEnvExpression(Expression variableName, Position start, Position end)
        implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitReadEnv(this);
    }
}
