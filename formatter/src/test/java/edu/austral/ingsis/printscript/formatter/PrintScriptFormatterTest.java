package edu.austral.ingsis.printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.OperatorPrecedence;
import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BooleanLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.IfStatement;
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
 * Unit tests for the formatter alone: the AST is built by hand here instead of going through a real
 * {@code Lexer}/{@code Parser} - {@code formatter} has no dependency (not even a test one) on
 * either module, so these tests only ever exercise the formatter's own rendering logic.
 */
class PrintScriptFormatterTest {

    private static final Position P = new Position(1, 1);

    private final PrintScriptFormatter formatter = new PrintScriptFormatter();
    private final FormatterConfigLoader configLoader = new FormatterConfigLoader();

    private static Expression num(double value) {
        return new NumberLiteralExpression(value, P, P);
    }

    private static Expression id(String name) {
        return new IdentifierExpression(name, P, P);
    }

    private static Expression str(String value) {
        return new StringLiteralExpression(value, P, P);
    }

    private static Expression bool(boolean value) {
        return new BooleanLiteralExpression(value, P, P);
    }

    private static Statement let(String name, String type, Expression initializer) {
        return new VariableDeclarationStatement(name, type, Optional.of(initializer), P, P);
    }

    private static Statement constDecl(String name, String type, Expression initializer) {
        return new VariableDeclarationStatement(name, type, true, Optional.of(initializer), P, P);
    }

    private static Statement assign(String name, Expression value) {
        return new AssignmentStatement(name, value, P, P);
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

    private static IdentifierExpression condition(String name) {
        return new IdentifierExpression(name, P, P);
    }

    private static Statement ifStmt(String conditionName, Statement... thenBranch) {
        return new IfStatement(
                condition(conditionName), List.of(thenBranch), Optional.empty(), P, P);
    }

    private static Statement ifElseStmt(
            String conditionName, List<Statement> thenBranch, List<Statement> elseBranch) {
        return new IfStatement(condition(conditionName), thenBranch, Optional.of(elseBranch), P, P);
    }

    private String format(FormatterConfig config, Statement... statements) {
        return formatter.format(List.of(statements).iterator(), config);
    }

    @Test
    void addsSpaceAroundColonAndEqualsWhenConfigured() {
        // let x: number = 12;
        FormatterConfig config = new FormatterConfig(true, true, true, 0);

        String result = format(config, let("x", "number", num(12)));

        assertEquals("let x : number = 12;\n", result);
    }

    @Test
    void omitsSpaceBeforeColonWhenConfigured() {
        // let x: number = 12;
        FormatterConfig config = new FormatterConfig(false, true, true, 0);

        String result = format(config, let("x", "number", num(12)));

        assertEquals("let x: number = 12;\n", result);
    }

    @Test
    void formatsAConstDeclarationWithTheConstKeyword() {
        // const x: number = 12;
        FormatterConfig config = new FormatterConfig(false, true, true, 0);

        String result = format(config, constDecl("x", "number", num(12)));

        assertEquals("const x: number = 12;\n", result);
    }

    @Test
    void insertsBlankLinesBeforePrintlnAsConfigured() {
        // let x: number = 1;
        // println(x);
        FormatterConfig config = new FormatterConfig(false, true, true, 2);

        String result = format(config, let("x", "number", num(1)), println(id("x")));

        assertEquals("let x: number = 1;\n\n\nprintln(x);\n", result);
    }

    @Test
    void alwaysPutsOneSpaceAroundOperatorsRegardlessOfConfig() {
        // x = 1 + 2;
        FormatterConfig config = new FormatterConfig(false, false, false, 0);

        String result =
                format(
                        config,
                        assign(
                                "x",
                                new BinaryExpression(num(1), CoreOperators.PLUS, num(2), P, P)));

        assertEquals("x=1 + 2;\n", result);
    }

    @Test
    void usesSingleQuotesWhenTheStringContainsADoubleQuote() {
        // println('he said "hi"');
        FormatterConfig config = FormatterConfig.defaultConfig();

        String result = format(config, println(str("he said \"hi\"")));

        assertEquals("println('he said \"hi\"');\n", result);
    }

    @Test
    void formatsABooleanLiteral() {
        // let flag: boolean = true;
        FormatterConfig config = new FormatterConfig(false, true, true, 0);

        String result = format(config, let("flag", "boolean", bool(true)));

        assertEquals("let flag: boolean = true;\n", result);
    }

    @Test
    void formatsAnIfBlockWithTheConfiguredIndent() {
        // if (flag) {
        //   println(x);
        // }
        FormatterConfig config = new FormatterConfig(false, true, true, 0, 2);

        String result = format(config, ifStmt("flag", println(id("x"))));

        assertEquals("if (flag) {\n  println(x);\n}\n", result);
    }

    @Test
    void formatsAnIfBlockWithADifferentIndentSize() {
        FormatterConfig config = new FormatterConfig(false, true, true, 0, 4);

        String result = format(config, ifStmt("flag", println(id("x"))));

        assertEquals("if (flag) {\n    println(x);\n}\n", result);
    }

    @Test
    void formatsAnIfElseBlockWithTheBraceOnTheSameLine() {
        // if (flag) {
        //   println(x);
        // } else {
        //   println(y);
        // }
        FormatterConfig config = new FormatterConfig(false, true, true, 0, 2);

        String result =
                format(
                        config,
                        ifElseStmt("flag", List.of(println(id("x"))), List.of(println(id("y")))));

        assertEquals("if (flag) {\n  println(x);\n} else {\n  println(y);\n}\n", result);
    }

    @Test
    void formatsMultipleStatementsInsideABlockEachOnItsOwnIndentedLine() {
        FormatterConfig config = new FormatterConfig(false, true, true, 0, 2);

        String result =
                format(config, ifStmt("flag", let("x", "number", num(1)), println(id("x"))));

        assertEquals("if (flag) {\n  let x: number = 1;\n  println(x);\n}\n", result);
    }

    @Test
    void formatsANestedIfWithAccumulatedIndentation() {
        // if (outer) {
        //   if (inner) {
        //     println(x);
        //   }
        // }
        FormatterConfig config = new FormatterConfig(false, true, true, 0, 2);

        String result = format(config, ifStmt("outer", ifStmt("inner", println(id("x")))));

        assertEquals("if (outer) {\n  if (inner) {\n    println(x);\n  }\n}\n", result);
    }

    @Test
    void doesNotInsertBlankLinesBeforePrintlnInsideABlock() {
        // newLinesBeforePrintln only applies at the top level, not inside if/else blocks.
        FormatterConfig config = new FormatterConfig(false, true, true, 2, 2);

        String result =
                format(config, ifStmt("flag", let("x", "number", num(1)), println(id("x"))));

        assertEquals("if (flag) {\n  let x: number = 1;\n  println(x);\n}\n", result);
    }

    @Test
    void loadsConfigFromYaml() {
        String yaml =
                """
                spaceBeforeColon: true
                spaceAfterColon: false
                spaceAroundEquals: false
                newLinesBeforePrintln: 1
                indentSize: 4
                """;

        FormatterConfig config = configLoader.load(new StringReader(yaml), ConfigFormat.YAML);

        assertEquals(new FormatterConfig(true, false, false, 1, 4), config);
    }

    @Test
    void indentSizeDefaultsToTheDomainDefaultWhenOmittedFromYaml() {
        // FormatterConfigLoader deserializes into RawFormatterConfig first (every field boxed, so
        // "absent" comes through as null, distinguishable from an explicit 0) and only then
        // resolves missing fields against FormatterConfig.defaultConfig() - so a field missing
        // from the YAML falls back to the real default (2), not Java's raw 0.
        String yaml =
                """
                spaceBeforeColon: true
                spaceAfterColon: false
                spaceAroundEquals: false
                newLinesBeforePrintln: 1
                """;

        FormatterConfig config = configLoader.load(new StringReader(yaml), ConfigFormat.YAML);

        assertEquals(2, config.indentSize());
    }

    @Test
    void formatsReadInputAndReadEnvCalls() {
        // let name: string = readInput("Your name:");
        // let port: number = readEnv("PORT");
        FormatterConfig config = new FormatterConfig(false, true, true, 0);

        String result =
                format(
                        config,
                        let("name", "string", readInput(str("Your name:"))),
                        let("port", "number", readEnv(str("PORT"))));

        assertEquals(
                "let name: string = readInput(\"Your name:\");\n"
                        + "let port: number = readEnv(\"PORT\");\n",
                result);
    }

    @Test
    void formatsAnOperatorContributedByAPlugin() {
        // x = a % b;
        OperatorDefinition modulo = stubModuloOperator();
        FormatterConfig config = new FormatterConfig(false, true, true, 0);

        String result =
                format(config, assign("x", new BinaryExpression(id("a"), modulo, id("b"), P, P)));

        assertEquals("x = a % b;\n", result);
    }

    private static OperatorDefinition stubModuloOperator() {
        return new OperatorDefinition() {
            @Override
            public String symbol() {
                return "%";
            }

            @Override
            public OperatorPrecedence precedence() {
                return OperatorPrecedence.higherThan(CoreOperators.PLUS, CoreOperators.MINUS);
            }

            @Override
            public double apply(double left, double right) {
                return left % right;
            }
        };
    }
}
