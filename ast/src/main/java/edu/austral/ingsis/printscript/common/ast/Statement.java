package edu.austral.ingsis.printscript.common.ast;

/**
 * A top-level, semicolon-terminated instruction: declaration, assignment or println call.
 *
 * <p>Intentionally NOT sealed: a new statement kind (e.g. a future {@code if}) is added by
 * implementing this interface from anywhere, with zero changes to this file or to any existing
 * {@link StatementVisitor} implementor. See the Acyclic Visitor pattern used by {@link
 * StatementVisitor} for how dispatch stays type-safe without a closed set of permitted
 * implementations.
 */
public non-sealed interface Statement extends ASTNode {

    <R> R accept(StatementVisitor<R> visitor);
}
