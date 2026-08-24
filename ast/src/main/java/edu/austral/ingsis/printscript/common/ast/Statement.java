package edu.austral.ingsis.printscript.common.ast;

/** A top-level, semicolon-terminated instruction: declaration, assignment or println call. */
public sealed interface Statement extends ASTNode
        permits VariableDeclarationStatement, AssignmentStatement, PrintlnStatement {

    <R> R accept(StatementVisitor<R> visitor);
}
