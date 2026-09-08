package edu.austral.ingsis.printscript.analyzer;

import java.util.Optional;

import edu.austral.ingsis.printscript.common.ast.Statement;

/**
 * A single, independent check against one statement. Each rule decides for itself which statement
 * types it cares about — the others just fall through with {@link Optional#empty()}.
 */
interface AnalysisRule {

    Optional<AnalysisFinding> check(Statement statement);
}
