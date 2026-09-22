package edu.austral.ingsis.printscript.common.ast;

import java.util.List;
import java.util.Optional;

import edu.austral.ingsis.printscript.common.Position;

/**
 * {@code if (<condition>) { <thenBranch> } [else { <elseBranch> }]}.
 *
 * <p>obs: {@code condition} is typed as {@link IdentifierExpression}, not {@link Expression}: the
 * grammar only accepts a boolean variable name there (no arbitrary boolean expressions) - "solo con
 * variables boolean como argumento". Whether that variable is actually of type {@code boolean} is
 * still a semantic property checked at runtime (types aren't tracked in the AST).
 */
public record IfStatement(
        IdentifierExpression condition,
        List<Statement> thenBranch,
        Optional<List<Statement>> elseBranch,
        Position start,
        Position end)
        implements Statement {

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitIf(this);
    }
}
