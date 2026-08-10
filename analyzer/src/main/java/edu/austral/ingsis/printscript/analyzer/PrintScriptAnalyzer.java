package edu.austral.ingsis.printscript.analyzer;

import edu.austral.ingsis.printscript.common.ast.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class PrintScriptAnalyzer implements Analyzer {

    @Override
    public List<AnalysisFinding> analyze(Iterator<Statement> statements, AnalyzerConfig config) {
        RuleChecker checker = new RuleChecker(config);
        List<AnalysisFinding> findings = new ArrayList<>();
        while (statements.hasNext()) {
            findings.addAll(statements.next().accept(checker));
        }
        return findings;
    }
}
