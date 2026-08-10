package edu.austral.ingsis.printscript.analyzer;

import edu.austral.ingsis.printscript.common.ast.Statement;
import java.util.Iterator;
import java.util.List;

/** Reports every rule violation in a program, without stopping at the first one. */
public interface Analyzer {

    List<AnalysisFinding> analyze(Iterator<Statement> statements, AnalyzerConfig config);
}
