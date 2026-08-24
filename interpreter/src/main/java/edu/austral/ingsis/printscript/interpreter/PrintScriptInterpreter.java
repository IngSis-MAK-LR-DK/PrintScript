package edu.austral.ingsis.printscript.interpreter;

import java.io.PrintStream;
import java.util.Iterator;

import edu.austral.ingsis.printscript.common.ast.Statement;

public final class PrintScriptInterpreter implements Interpreter {

    @Override
    public void interpret(Iterator<Statement> statements, PrintStream output) {
        Environment environment = new Environment();
        StatementExecutor executor = new StatementExecutor(environment, output);
        while (statements.hasNext()) {
            statements.next().accept(executor);
        }
    }
}
