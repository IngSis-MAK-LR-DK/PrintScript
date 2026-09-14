package edu.austral.ingsis.printscript.interpreter;

import java.util.Iterator;

import edu.austral.ingsis.printscript.common.ast.Statement;

public final class PrintScriptInterpreter implements Interpreter {

    @Override
    public void interpret(Iterator<Statement> statements, ExecutionContext context) {
        Environment environment = new Environment();
        while (statements.hasNext()) {
            StatementExecutor executor = new StatementExecutor(environment, context);
            environment = statements.next().accept(executor);
        }
    }
}
