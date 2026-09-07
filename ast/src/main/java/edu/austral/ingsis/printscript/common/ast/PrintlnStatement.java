package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

/** {@code println(<argument>);} */
public record PrintlnStatement(Expression argument, Position start, Position end)
        implements Statement {

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        if (visitor instanceof PrintlnVisitor<R> v) {
            return v.visitPrintln(this);
        }
        throw new UnsupportedOperationException(
                visitor.getClass().getName() + " does not support PrintlnStatement");
    }
}
