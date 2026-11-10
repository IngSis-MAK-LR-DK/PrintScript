package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SourceRange;

/**
 * {@code identifierName = value;}
 *
 * @param identifierRange where the variable's name sits, so a problem with the name can point at
 *     the name and not at the whole statement
 */
public record AssignmentStatement(
        String identifierName,
        Expression value,
        SourceRange identifierRange,
        Position start,
        Position end)
        implements Statement {

    /** For callers that have no finer location than the statement itself. */
    public AssignmentStatement(
            String identifierName, Expression value, Position start, Position end) {
        this(identifierName, value, new SourceRange(start, end), start, end);
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitAssignment(this);
    }
}
