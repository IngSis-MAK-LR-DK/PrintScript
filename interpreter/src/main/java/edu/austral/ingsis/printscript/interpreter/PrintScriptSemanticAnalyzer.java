package edu.austral.ingsis.printscript.interpreter;

import java.util.Iterator;

import edu.austral.ingsis.printscript.common.ast.Statement;

/**
 * Checks a program's semantics by running it through a real {@link PrintScriptInterpreter} with its
 * {@code println} output discarded. PrintScript 1.0 has no loops or external input, so every
 * declaration and assignment is checked by simply evaluating it — there's no cheaper way to know a
 * program type-checks than to run it. That's an implementation detail of this one class: nothing
 * that depends on {@link SemanticAnalyzer} needs to know the check happens to work this way.
 *
 * <p>{@code readInput}/{@code readEnv} deliberately aren't backed by anything real here (see {@link
 * InputProvider#unsupported()}/{@link EnvironmentReader#unsupported()}) - validation shouldn't
 * block on real standard input or depend on which environment variables happen to be set wherever
 * it runs. A program that uses either surfaces a clear {@code SemanticException} instead.
 */
public final class PrintScriptSemanticAnalyzer implements SemanticAnalyzer {

    private final Interpreter interpreter = new PrintScriptInterpreter();

    @Override
    public void analyze(Iterator<Statement> statements) {
        interpreter.interpret(statements, ExecutionContext.withEmitter(line -> {}));
    }
}
