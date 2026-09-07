package edu.austral.ingsis.printscript.common.ast;

/**
 * Implemented by a {@link StatementVisitor} that knows how to handle an {@link
 * AssignmentStatement}.
 */
public interface AssignmentVisitor<R> extends StatementVisitor<R> {

    R visitAssignment(AssignmentStatement statement);
}
