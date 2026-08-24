package edu.austral.ingsis.printscript.analyzer;

import java.util.Iterator;
import java.util.List;

import edu.austral.ingsis.printscript.common.ast.Statement;

/** Reports every rule violation in a program, without stopping at the first one. */
public interface Analyzer {

    List<AnalysisFinding> analyze(Iterator<Statement> statements, AnalyzerConfig config);
}
