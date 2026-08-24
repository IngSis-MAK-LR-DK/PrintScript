package edu.austral.ingsis.printscript.cli;

import edu.austral.ingsis.printscript.analyzer.AnalyzerConfigLoader;
import edu.austral.ingsis.printscript.analyzer.PrintScriptAnalyzer;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.PrintScriptException;
import edu.austral.ingsis.printscript.formatter.FormatterConfigLoader;
import edu.austral.ingsis.printscript.formatter.PrintScriptFormatter;
import edu.austral.ingsis.printscript.interpreter.PrintScriptInterpreter;
import edu.austral.ingsis.printscript.lexer.PrintScriptLexer;
import edu.austral.ingsis.printscript.parser.PrintScriptParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * CLI entry point and composition root: this is the only place where concrete implementations of
 * {@code Lexer}, {@code Parser}, {@code Interpreter}, {@code Formatter} and {@code Analyzer} get
 * instantiated. Everything downstream ({@link Pipeline}, the {@link Command} implementations)
 * only ever sees their interfaces. It is also the only place that calls {@link ServiceLoader} to
 * discover operator plugins — the core modules never know which plugins, if any, are present.
 */
public final class Main {

    public static void main(String[] args) {
        try {
            CliArguments arguments = CliArguments.parse(args);
            Command command = commandFor(arguments);
            int exitCode = command.run(arguments);
            if (exitCode != 0) {
                System.exit(exitCode);
            }
        } catch (CliUsageException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        } catch (PrintScriptException e) {
            printError(e);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Could not read the source file: " + e.getMessage());
            System.exit(1);
        } catch (UncheckedIOException e) {
            System.err.println("Could not read the source file: " + e.getCause().getMessage());
            System.exit(1);
        }
    }

    private static Command commandFor(CliArguments arguments) {
        Set<OperatorDefinition> extensionOperators = discoverOperatorPlugins();
        Pipeline pipeline =
                new Pipeline(new PrintScriptLexer(extensionOperators), new PrintScriptParser(extensionOperators));

        return switch (arguments.operation()) {
            case VALIDATION -> new ValidationCommand(pipeline, new PrintScriptInterpreter());
            case EXECUTION -> new ExecutionCommand(pipeline, new PrintScriptInterpreter());
            case FORMATTING -> new FormattingCommand(pipeline, new PrintScriptFormatter(), new FormatterConfigLoader());
            case ANALYZING -> new AnalyzingCommand(pipeline, new PrintScriptAnalyzer(), new AnalyzerConfigLoader());
        };
    }

    private static Set<OperatorDefinition> discoverOperatorPlugins() {
        Set<OperatorDefinition> operators = new HashSet<>();
        ServiceLoader.load(OperatorDefinition.class).forEach(operators::add);
        return operators;
    }

    private static void printError(PrintScriptException e) {
        System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        System.err.printf(
                "  at line %d, column %d to line %d, column %d%n",
                e.start().line(), e.start().column(), e.end().line(), e.end().column());
    }
}
