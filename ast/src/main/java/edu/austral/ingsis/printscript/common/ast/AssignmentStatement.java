package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

/** {@code identifierName = value;} */
public record AssignmentStatement(
        String identifierName, Expression value, Position start, Position end)
        implements Statement {

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        if (visitor instanceof AssignmentVisitor<R> v) {
            return v.visitAssignment(this);
        }
        throw new UnsupportedOperationException(
                visitor.getClass().getName() + " does not support AssignmentStatement");
    }
}
