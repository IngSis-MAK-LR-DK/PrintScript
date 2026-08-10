package edu.austral.ingsis.printscript.common.ast;

/** Double-dispatch contract implemented by the interpreter, formatter and analyzer. */
public interface StatementVisitor<R> {

    R visitVariableDeclaration(VariableDeclarationStatement statement);

    R visitAssignment(AssignmentStatement statement);

    R visitPrintln(PrintlnStatement statement);
}
