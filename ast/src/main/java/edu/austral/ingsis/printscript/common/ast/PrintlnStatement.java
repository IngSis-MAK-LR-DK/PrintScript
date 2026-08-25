package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

/** {@code println(<argument>);} */
public record PrintlnStatement(Expression argument, Position start, Position end)
        implements Statement {

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitPrintln(this);
    }
}
