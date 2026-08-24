package edu.austral.ingsis.printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BinaryOperator;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.ExtendedBinaryExpression;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;
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

    private static Statement let(String name, String type, Expression initializer) {
        return new VariableDeclarationStatement(name, type, Optional.of(initializer), P, P);
    }

    private static Statement assign(String name, Expression value) {
        return new AssignmentStatement(name, value, P, P);
    }

    private static Statement println(Expression argument) {
        return new PrintlnStatement(argument, P, P);
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
                                new BinaryExpression(num(1), BinaryOperator.PLUS, num(2), P, P)));

        assertEquals("x=1 + 2;\n", result);
    }

    @Test
    void loadsConfigFromYaml() {
        String yaml =
                """
                spaceBeforeColon: true
                spaceAfterColon: false
                spaceAroundEquals: false
                newLinesBeforePrintln: 1
                """;

        FormatterConfig config = configLoader.load(new StringReader(yaml), ConfigFormat.YAML);

        assertEquals(new FormatterConfig(true, false, false, 1), config);
    }

    @Test
    void formatsAnOperatorContributedByAPlugin() {
        // x = a % b;
        OperatorDefinition modulo = stubModuloOperator();
        FormatterConfig config = new FormatterConfig(false, true, true, 0);

        String result =
                format(
                        config,
                        assign("x", new ExtendedBinaryExpression(id("a"), modulo, id("b"), P, P)));

        assertEquals("x = a % b;\n", result);
    }

    private static OperatorDefinition stubModuloOperator() {
        return new OperatorDefinition() {
            @Override
            public String symbol() {
                return "%";
            }

            @Override
            public double apply(double left, double right) {
                return left % right;
            }
        };
    }
}
