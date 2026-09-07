package edu.austral.ingsis.printscript.common.ast;

/**
 * A node that produces a value: a literal, an identifier reference, or a binary operation.
 *
 * <p>Intentionally NOT sealed: a new expression kind (e.g. a future {@code readInput}) is added by
 * implementing this interface from anywhere, with zero changes to this file or to any existing
 * {@link ExpressionVisitor} implementor. See the Acyclic Visitor pattern used by {@link
 * ExpressionVisitor} for how dispatch stays type-safe without a closed set of permitted
 * implementations.
 */
public non-sealed interface Expression extends ASTNode {

    <R> R accept(ExpressionVisitor<R> visitor);
}
