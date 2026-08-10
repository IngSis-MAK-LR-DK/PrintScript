package edu.austral.ingsis.printscript.cli;

import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.interpreter.Interpreter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;

/** Runs the interpreter with its output discarded, so only syntax/semantic errors surface. */
final class ValidationCommand implements Command {

    private final Pipeline pipeline;
    private final Interpreter interpreter;

    ValidationCommand(Pipeline pipeline, Interpreter interpreter) {
        this.pipeline = pipeline;
        this.interpreter = interpreter;
    }

    @Override
    public int run(CliArguments arguments) throws IOException {
        Iterator<Statement> statements = pipeline.parse(arguments.sourceFile());
        interpreter.interpret(statements, new PrintStream(OutputStream.nullOutputStream()));
        System.out.println("OK: no syntax or semantic errors found");
        return 0;
    }
}
