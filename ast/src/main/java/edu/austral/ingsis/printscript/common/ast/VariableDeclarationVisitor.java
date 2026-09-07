package edu.austral.ingsis.printscript.common.ast;

/**
 * Implemented by a {@link StatementVisitor} that knows how to handle a {@link
 * VariableDeclarationStatement}.
 */
public interface VariableDeclarationVisitor<R> extends StatementVisitor<R> {

    R visitVariableDeclaration(VariableDeclarationStatement statement);
}
