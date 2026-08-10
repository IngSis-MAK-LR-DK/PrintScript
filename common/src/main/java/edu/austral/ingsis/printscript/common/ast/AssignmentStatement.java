package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

/** {@code <identifierName> = <value>;} */
public record AssignmentStatement(String identifierName, Expression value, Position start, Position end)
        implements Statement {

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitAssignment(this);
    }
}
