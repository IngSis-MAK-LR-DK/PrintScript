package edu.austral.ingsis.printscript.analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.austral.ingsis.printscript.common.ast.Statement;

public final class PrintScriptAnalyzer implements Analyzer {

    @Override
    public List<AnalysisFinding> analyze(Iterator<Statement> statements, AnalyzerConfig config) {
        List<AnalysisRule> rules = config.toRules();
        List<AnalysisFinding> findings = new ArrayList<>();
        while (statements.hasNext()) {
            Statement statement = statements.next();
            for (AnalysisRule rule : rules) {
                rule.check(statement).ifPresent(findings::add);
            }
        }
        return findings;
    }
}
