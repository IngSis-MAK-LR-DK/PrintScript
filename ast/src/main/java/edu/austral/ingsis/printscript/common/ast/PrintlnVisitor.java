package edu.austral.ingsis.printscript.common.ast;

/**
 * Implemented by a {@link StatementVisitor} that knows how to handle a {@link PrintlnStatement}.
 */
public interface PrintlnVisitor<R> extends StatementVisitor<R> {

    R visitPrintln(PrintlnStatement statement);
}
