package edu.austral.ingsis.printscript.interpreter;

import java.util.Iterator;

import edu.austral.ingsis.printscript.common.ast.Statement;

/**
 * Checks a program's semantics by walking its AST, without running any of it. That's what lets it
 * accept {@code readInput}/{@code readEnv} without blocking on real input, and check both branches
 * of an {@code if} instead of only the one a run would take.
 */
public final class PrintScriptSemanticAnalyzer implements SemanticAnalyzer {

    @Override
    public void analyze(Iterator<Statement> statements) {
        StatementTypeChecker checker = new StatementTypeChecker(new TypeScope());
        while (statements.hasNext()) {
            statements.next().accept(checker);
        }
    }
}
