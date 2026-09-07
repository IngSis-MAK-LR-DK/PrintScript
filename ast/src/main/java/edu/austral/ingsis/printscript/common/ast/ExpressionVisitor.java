package edu.austral.ingsis.printscript.common.ast;

/**
 * Marker interface for anything that can visit an {@link Expression}. Deliberately empty (Acyclic
 * Visitor pattern): each expression kind has its own single-method interface ({@link
 * NumberLiteralVisitor}, {@link StringLiteralVisitor}, {@link IdentifierVisitor}, {@link
 * BinaryVisitor}, and any added later), and {@code Expression.accept(...)} dispatches to whichever
 * of those the passed-in visitor actually implements. A class implements only the sub-interfaces
 * for the expression kinds it cares about - adding a new expression kind never forces existing
 * implementors to change.
 */
public interface ExpressionVisitor<R> {}
