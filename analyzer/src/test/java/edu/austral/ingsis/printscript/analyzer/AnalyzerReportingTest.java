package edu.austral.ingsis.printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SourceRange;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

import org.junit.jupiter.api.Test;

class AnalyzerReportingTest {

    private static final Position STATEMENT_START = new Position(1, 1);
    private static final Position STATEMENT_END = new Position(1, 24);
    private static final SourceRange NAME =
            new SourceRange(new Position(1, 5), new Position(1, 11));
    private static final SourceRange TYPE =
            new SourceRange(new Position(1, 13), new Position(1, 19));

    private final Analyzer analyzer = new PrintScriptAnalyzer();

    private static Statement badlyNamedDeclaration() {
        return new VariableDeclarationStatement(
                "my_var",
                "number",
                false,
                Optional.of(new NumberLiteralExpression(1, STATEMENT_START, STATEMENT_START)),
                NAME,
                TYPE,
                STATEMENT_START,
                STATEMENT_END);
    }

    @Test
    void namingViolationPointsAtTheIdentifierOnly() {
        List<AnalysisFinding> findings =
                analyzer.analyze(
                        List.of(badlyNamedDeclaration()).iterator(),
                        AnalyzerConfig.defaultConfig());

        assertEquals(NAME.start(), findings.get(0).start());
        assertEquals(NAME.end(), findings.get(0).end());
    }

    @Test
    void reportsEachViolationToTheReporter() {
        List<AnalysisFinding> reported = new ArrayList<>();

        analyzer.analyze(
                List.of(badlyNamedDeclaration(), badlyNamedDeclaration()).iterator(),
                AnalyzerConfig.defaultConfig(),
                reported::add);

        assertEquals(2, reported.size());
    }
}
