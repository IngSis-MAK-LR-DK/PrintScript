package edu.austral.ingsis.printscript.common.ast;

import java.util.List;
import java.util.Optional;

import edu.austral.ingsis.printscript.common.Position;

/**
 * {@code if (<condition>) { <thenBranch> } [else { <elseBranch> }]}.
 *
 * <p>{@code condition} is typed as {@link IdentifierExpression}, not {@link Expression}: the
 * grammar only ever accepts a bare variable name there (no arbitrary boolean expressions) - "solo
 * con variables boolean como argumento" - so this is enforced by construction, not by a separate
 * syntax check. Whether that variable is actually of type {@code boolean} is still a semantic
 * property checked at runtime (types aren't tracked in the AST).
 *
 * <p>{@code elseBranch} is {@code Optional.empty()} when the source has no {@code else} clause at
 * all - there's no "else if" (per the consigna), so a chain of conditions is just nested {@code
 * IfStatement}s inside an {@code elseBranch}.
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
