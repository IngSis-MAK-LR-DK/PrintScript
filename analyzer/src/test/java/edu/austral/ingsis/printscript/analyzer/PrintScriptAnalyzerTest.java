package edu.austral.ingsis.printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.austral.ingsis.printscript.common.ConfigFormat;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.lexer.PrintScriptLexer;
import edu.austral.ingsis.printscript.parser.PrintScriptParser;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;

class PrintScriptAnalyzerTest {

    private final PrintScriptLexer lexer = new PrintScriptLexer();
    private final PrintScriptParser parser = new PrintScriptParser();
    private final PrintScriptAnalyzer analyzer = new PrintScriptAnalyzer();
    private final AnalyzerConfigLoader configLoader = new AnalyzerConfigLoader();

    private List<AnalysisFinding> analyze(String source, AnalyzerConfig config) {
        Iterator<Statement> statements = parser.parse(lexer.tokenize(new StringReader(source)));
        return analyzer.analyze(statements, config);
    }

    @Test
    void reportsNothingForCompliantCamelCaseCode() {
        AnalyzerConfig config = AnalyzerConfig.defaultConfig();

        List<AnalysisFinding> findings = analyze("let myVariable: number = 1;\nprintln(myVariable);", config);

        assertTrue(findings.isEmpty());
    }

    @Test
    void reportsSnakeCaseIdentifierWhenCamelCaseIsRequired() {
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, true);

        List<AnalysisFinding> findings = analyze("let my_variable: number = 1;", config);

        assertEquals(1, findings.size());
        assertTrue(findings.get(0).message().contains("my_variable"));
    }

    @Test
    void reportsCamelCaseIdentifierWhenSnakeCaseIsRequired() {
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.SNAKE_CASE, true);

        List<AnalysisFinding> findings = analyze("let myVariable: number = 1;", config);

        assertEquals(1, findings.size());
    }

    @Test
    void skipsIdentifierCaseCheckWhenDisabled() {
        AnalyzerConfig config = new AnalyzerConfig(false, IdentifierCase.CAMEL_CASE, true);

        List<AnalysisFinding> findings = analyze("let my_variable: number = 1;", config);

        assertTrue(findings.isEmpty());
    }

    @Test
    void reportsPrintlnCalledWithAnExpression() {
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, true);

        List<AnalysisFinding> findings = analyze("let a: number = 1;\nprintln(a + 1);", config);

        assertEquals(1, findings.size());
        assertTrue(findings.get(0).message().contains("println"));
    }

    @Test
    void allowsPrintlnCalledWithALiteralOrIdentifier() {
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, true);

        List<AnalysisFinding> findings = analyze("println(\"hello\");", config);

        assertTrue(findings.isEmpty());
    }

    @Test
    void skipsPrintlnArgumentCheckWhenDisabled() {
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, false);

        List<AnalysisFinding> findings = analyze("let a: number = 1;\nprintln(a + 1);", config);

        assertTrue(findings.isEmpty());
    }

    @Test
    void loadsConfigFromYaml() {
        String yaml =
                """
                identifierCaseCheckEnabled: true
                identifierCase: SNAKE_CASE
                printlnArgumentMustBeIdentifierOrLiteral: false
                """;

        AnalyzerConfig config = configLoader.load(new StringReader(yaml), ConfigFormat.YAML);

        assertEquals(new AnalyzerConfig(true, IdentifierCase.SNAKE_CASE, false), config);
    }
}
