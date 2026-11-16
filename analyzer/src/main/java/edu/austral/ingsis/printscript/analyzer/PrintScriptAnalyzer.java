package edu.austral.ingsis.printscript.analyzer;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import edu.austral.ingsis.printscript.common.ast.IfStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;

public final class PrintScriptAnalyzer implements Analyzer {

    @Override
    public void analyze(
            Iterator<Statement> statements,
            AnalyzerConfig config,
            Consumer<AnalysisFinding> reporter) {
        List<AnalysisRule> rules = config.toRules();
        while (statements.hasNext()) {
            analyzeStatement(statements.next(), rules, reporter);
        }
    }

    /**
     * Applies every rule to {@code statement}, then - if it's an {@code if} - recurses into its
     * branches too, so a violation inside a block gets reported just like one at the top level.
     */
    private static void analyzeStatement(
            Statement statement, List<AnalysisRule> rules, Consumer<AnalysisFinding> reporter) {
        for (AnalysisRule rule : rules) {
            rule.check(statement).ifPresent(reporter);
        }
        if (statement instanceof IfStatement ifStatement) {
            ifStatement.thenBranch().forEach(inner -> analyzeStatement(inner, rules, reporter));
            ifStatement
                    .elseBranch()
                    .ifPresent(
                            branch ->
                                    branch.forEach(
                                            inner -> analyzeStatement(inner, rules, reporter)));
        }
    }
}
