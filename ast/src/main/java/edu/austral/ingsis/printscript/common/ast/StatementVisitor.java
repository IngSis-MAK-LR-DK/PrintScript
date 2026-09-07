package edu.austral.ingsis.printscript.common.ast;

/**
 * Marker interface for anything that can visit a {@link Statement}. Deliberately empty (Acyclic
 * Visitor pattern): each statement kind has its own single-method interface ({@link
 * VariableDeclarationVisitor}, {@link AssignmentVisitor}, {@link PrintlnVisitor}, and any added
 * later), and {@code Statement.accept(...)} dispatches to whichever of those the passed-in visitor
 * actually implements. A class implements only the sub-interfaces for the statement kinds it cares
 * about - adding a new statement kind never forces existing implementors to change.
 */
public interface StatementVisitor<R> {}
