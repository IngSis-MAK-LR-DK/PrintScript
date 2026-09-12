package edu.austral.ingsis.printscript.cli;

import java.io.IOException;
import java.util.Iterator;

import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.interpreter.SemanticAnalyzer;

/** Checks a program's semantics, without running it for its actual output. */
final class ValidationCommand implements Command {

    private final Pipeline pipeline;
    private final SemanticAnalyzer analyzer;

    ValidationCommand(Pipeline pipeline, SemanticAnalyzer analyzer) {
        this.pipeline = pipeline;
        this.analyzer = analyzer;
    }

    @Override
    public int run(CliArguments arguments) throws IOException {
        Iterator<Statement> statements = pipeline.parse(arguments.sourceFile());
        analyzer.analyze(statements);
        System.out.println("OK: no syntax or semantic errors found");
        return 0;
    }
}
