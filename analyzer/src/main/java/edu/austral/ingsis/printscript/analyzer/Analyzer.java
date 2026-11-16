package edu.austral.ingsis.printscript.analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import edu.austral.ingsis.printscript.common.ast.Statement;

/** Reports every rule violation in a program, without stopping at the first one. */
public interface Analyzer {

    /**
     * Hands each violation to {@code reporter} as soon as it's found, so a program with more
     * violations than fit in memory can still be analyzed as long as {@code reporter} doesn't keep
     * them all.
     */
    void analyze(
            Iterator<Statement> statements,
            AnalyzerConfig config,
            Consumer<AnalysisFinding> reporter);

    /** Convenience for programs whose violations can all be held in memory at once. */
    default List<AnalysisFinding> analyze(Iterator<Statement> statements, AnalyzerConfig config) {
        List<AnalysisFinding> findings = new ArrayList<>();
        analyze(statements, config, findings::add);
        return findings;
    }
}
