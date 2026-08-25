package edu.austral.ingsis.printscript.cli;

import java.io.IOException;
import java.util.Iterator;

import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.interpreter.Interpreter;

final class ExecutionCommand implements Command {

    private final Pipeline pipeline;
    private final Interpreter interpreter;

    ExecutionCommand(Pipeline pipeline, Interpreter interpreter) {
        this.pipeline = pipeline;
        this.interpreter = interpreter;
    }

    @Override
    public int run(CliArguments arguments) throws IOException {
        Iterator<Statement> statements = pipeline.parse(arguments.sourceFile());
        interpreter.interpret(statements, System.out);
        return 0;
    }
}
