package edu.austral.ingsis.printscript.common.ast;

/** A semicolon-terminated instruction: declaration, assignment or println. */
public sealed interface Statement extends ASTNode
        permits VariableDeclarationStatement, AssignmentStatement, PrintlnStatement {

    <R> R accept(StatementVisitor<R> visitor);
}
