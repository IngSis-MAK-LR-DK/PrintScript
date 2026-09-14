package edu.austral.ingsis.printscript.analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.austral.ingsis.printscript.common.ast.IfStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;

public final class PrintScriptAnalyzer implements Analyzer {

    @Override
    public List<AnalysisFinding> analyze(Iterator<Statement> statements, AnalyzerConfig config) {
        List<AnalysisRule> rules = config.toRules();
        List<AnalysisFinding> findings = new ArrayList<>();
        while (statements.hasNext()) {
            analyzeStatement(statements.next(), rules, findings);
        }
        return findings;
    }

    /**
     * Applies every rule to {@code statement}, then - if it's an {@code if} - recurses into its
     * branches too, so a violation inside a block gets reported just like one at the top level.
     */
    private static void analyzeStatement(
            Statement statement, List<AnalysisRule> rules, List<AnalysisFinding> findings) {
        for (AnalysisRule rule : rules) {
            rule.check(statement).ifPresent(findings::add);
        }
        if (statement instanceof IfStatement ifStatement) {
            ifStatement.thenBranch().forEach(inner -> analyzeStatement(inner, rules, findings));
            ifStatement
                    .elseBranch()
                    .ifPresent(
                            branch ->
                                    branch.forEach(
                                            inner -> analyzeStatement(inner, rules, findings)));
        }
    }
}
