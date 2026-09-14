package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

/**
 * {@code readInput(<message>)}. Always evaluates to a raw {@code String} at runtime (see the
 * interpreter's {@code ExpressionEvaluator}); coercion to the target variable's declared type
 * happens one level up, in the interpreter's statement executor, only when this node is the direct
 * initializer/value of a declaration or assignment - used any other way (e.g. as {@code println}'s
 * direct argument) it stays a plain string, which is already the correct behavior there.
 */
public record ReadInputExpression(Expression message, Position start, Position end)
        implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitReadInput(this);
    }
}
