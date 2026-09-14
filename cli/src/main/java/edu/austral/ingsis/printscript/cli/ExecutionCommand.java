package edu.austral.ingsis.printscript.cli;

import java.io.IOException;
import java.util.Iterator;

import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.interpreter.EnvironmentReader;
import edu.austral.ingsis.printscript.interpreter.ExecutionContext;
import edu.austral.ingsis.printscript.interpreter.InputProvider;
import edu.austral.ingsis.printscript.interpreter.Interpreter;

final class ExecutionCommand implements Command {

    private final Pipeline pipeline;
    private final Interpreter interpreter;
    private final InputProvider inputProvider;
    private final EnvironmentReader environmentReader;

    ExecutionCommand(
            Pipeline pipeline,
            Interpreter interpreter,
            InputProvider inputProvider,
            EnvironmentReader environmentReader) {
        this.pipeline = pipeline;
        this.interpreter = interpreter;
        this.inputProvider = inputProvider;
        this.environmentReader = environmentReader;
    }

    @Override
    public int run(CliArguments arguments) throws IOException {
        Iterator<Statement> statements = pipeline.parse(arguments.sourceFile());
        interpreter.interpret(
                statements,
                new ExecutionContext(System.out::println, inputProvider, environmentReader));
        return 0;
    }
}
