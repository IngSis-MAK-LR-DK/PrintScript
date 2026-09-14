package edu.austral.ingsis.printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BooleanLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.ReadEnvExpression;
import edu.austral.ingsis.printscript.common.ast.ReadInputExpression;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;
import edu.austral.ingsis.printscript.config.ConfigFormat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the analyzer alone: the AST is built by hand here instead of going through a real
 * {@code Lexer}/{@code Parser} - {@code analyzer} has no dependency (not even a test one) on either
 * module, so these tests only ever exercise the analyzer's own rule-checking logic.
 */
class PrintScriptAnalyzerTest {

    private static final Position P = new Position(1, 1);

    private final PrintScriptAnalyzer analyzer = new PrintScriptAnalyzer();
    private final AnalyzerConfigLoader configLoader = new AnalyzerConfigLoader();

    private static Expression num(double value) {
        return new NumberLiteralExpression(value, P, P);
    }

    private static Expression str(String value) {
        return new StringLiteralExpression(value, P, P);
    }

    private static Expression id(String name) {
        return new IdentifierExpression(name, P, P);
    }

    private static Expression bool(boolean value) {
        return new BooleanLiteralExpression(value, P, P);
    }

    private static Statement let(String name, String type, Expression initializer) {
        return new VariableDeclarationStatement(name, type, Optional.of(initializer), P, P);
    }

    private static Statement println(Expression argument) {
        return new PrintlnStatement(argument, P, P);
    }

    private static Expression readInput(Expression message) {
        return new ReadInputExpression(message, P, P);
    }

    private static Expression readEnv(Expression variableName) {
        return new ReadEnvExpression(variableName, P, P);
    }

    private List<AnalysisFinding> analyze(AnalyzerConfig config, Statement... statements) {
        return analyzer.analyze(List.of(statements).iterator(), config);
    }

    @Test
    void reportsNothingForCompliantCamelCaseCode() {
        // let myVariable: number = 1;
        // println(myVariable);
        AnalyzerConfig config = AnalyzerConfig.defaultConfig();

        List<AnalysisFinding> findings =
                analyze(config, let("myVariable", "number", num(1)), println(id("myVariable")));

        assertTrue(findings.isEmpty());
    }

    @Test
    void reportsSnakeCaseIdentifierWhenCamelCaseIsRequired() {
        // let my_variable: number = 1;
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, true);

        List<AnalysisFinding> findings = analyze(config, let("my_variable", "number", num(1)));

        assertEquals(1, findings.size());
        assertTrue(findings.get(0).message().contains("my_variable"));
    }

    @Test
    void reportsCamelCaseIdentifierWhenSnakeCaseIsRequired() {
        // let myVariable: number = 1;
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.SNAKE_CASE, true);

        List<AnalysisFinding> findings = analyze(config, let("myVariable", "number", num(1)));

        assertEquals(1, findings.size());
    }

    @Test
    void skipsIdentifierCaseCheckWhenDisabled() {
        // let my_variable: number = 1;
        AnalyzerConfig config = new AnalyzerConfig(false, IdentifierCase.CAMEL_CASE, true);

        List<AnalysisFinding> findings = analyze(config, let("my_variable", "number", num(1)));

        assertTrue(findings.isEmpty());
    }

    @Test
    void reportsPrintlnCalledWithAnExpression() {
        // let a: number = 1;
        // println(a + 1);
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, true);

        List<AnalysisFinding> findings =
                analyze(
                        config,
                        let("a", "number", num(1)),
                        println(new BinaryExpression(id("a"), CoreOperators.PLUS, num(1), P, P)));

        assertEquals(1, findings.size());
        assertTrue(findings.get(0).message().contains("println"));
    }

    @Test
    void allowsPrintlnCalledWithALiteralOrIdentifier() {
        // println("hello");
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, true);

        List<AnalysisFinding> findings = analyze(config, println(str("hello")));

        assertTrue(findings.isEmpty());
    }

    @Test
    void allowsPrintlnCalledWithABooleanLiteral() {
        // println(true);
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, true);

        List<AnalysisFinding> findings = analyze(config, println(bool(true)));

        assertTrue(findings.isEmpty());
    }

    @Test
    void skipsPrintlnArgumentCheckWhenDisabled() {
        // let a: number = 1;
        // println(a + 1);
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, false);

        List<AnalysisFinding> findings =
                analyze(
                        config,
                        let("a", "number", num(1)),
                        println(new BinaryExpression(id("a"), CoreOperators.PLUS, num(1), P, P)));

        assertTrue(findings.isEmpty());
    }

    @Test
    void loadsConfigFromYaml() {
        String yaml =
                """
                identifierCaseCheckEnabled: true
                identifierCase: SNAKE_CASE
                printlnArgumentMustBeIdentifierOrLiteral: false
                readArgumentMustBeIdentifierOrLiteral: false
                """;

        AnalyzerConfig config = configLoader.load(new StringReader(yaml), ConfigFormat.YAML);

        assertEquals(new AnalyzerConfig(true, IdentifierCase.SNAKE_CASE, false, false), config);
    }

    @Test
    void readArgumentDefaultsToFalseWhenOmittedFromYaml() {
        // Jackson always deserializes a record through its canonical constructor, never through
        // AnalyzerConfig's 3-arg compatibility constructor (whose default is `true`) - so a field
        // missing from the YAML gets Java's raw `false` default instead. Same gotcha already
        // documented for FormatterConfig.indentSize.
        String yaml =
                """
                identifierCaseCheckEnabled: true
                identifierCase: SNAKE_CASE
                printlnArgumentMustBeIdentifierOrLiteral: false
                """;

        AnalyzerConfig config = configLoader.load(new StringReader(yaml), ConfigFormat.YAML);

        assertEquals(false, config.readArgumentMustBeIdentifierOrLiteral());
    }

    @Test
    void reportsReadInputCalledWithAnExpression() {
        // let a: number = 1;
        // let name: string = readInput("Name " + a);
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, true, true);

        List<AnalysisFinding> findings =
                analyze(
                        config,
                        let("a", "number", num(1)),
                        let(
                                "name",
                                "string",
                                readInput(
                                        new BinaryExpression(
                                                str("Name "), CoreOperators.PLUS, id("a"), P, P))));

        assertEquals(1, findings.size());
        assertTrue(findings.get(0).message().contains("readInput"));
    }

    @Test
    void allowsReadInputCalledWithALiteralOrIdentifier() {
        // let name: string = readInput("Name:");
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, true, true);

        List<AnalysisFinding> findings =
                analyze(config, let("name", "string", readInput(str("Name:"))));

        assertTrue(findings.isEmpty());
    }

    @Test
    void reportsReadEnvCalledWithAnExpression() {
        // let a: string = "POR" + "T";
        // let port: number = readEnv(a + "T");
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, true, true);

        List<AnalysisFinding> findings =
                analyze(
                        config,
                        let("a", "string", str("POR")),
                        let(
                                "port",
                                "number",
                                readEnv(
                                        new BinaryExpression(
                                                id("a"), CoreOperators.PLUS, str("T"), P, P))));

        assertEquals(1, findings.size());
        assertTrue(findings.get(0).message().contains("readEnv"));
    }

    @Test
    void skipsReadArgumentCheckWhenDisabled() {
        // let name: string = readInput("Name " + "extra");
        AnalyzerConfig config = new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, true, false);

        List<AnalysisFinding> findings =
                analyze(
                        config,
                        let(
                                "name",
                                "string",
                                readInput(
                                        new BinaryExpression(
                                                str("Name "),
                                                CoreOperators.PLUS,
                                                str("extra"),
                                                P,
                                                P))));

        assertTrue(findings.isEmpty());
    }
}
