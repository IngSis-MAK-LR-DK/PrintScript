package edu.austral.ingsis.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SemanticException;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BinaryOperator;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.ExtendedBinaryExpression;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the interpreter alone: the AST is built by hand here instead of going through a
 * real {@code Lexer}/{@code Parser} - {@code interpreter} has no dependency (not even a test one)
 * on either module, so these tests only ever exercise the interpreter's own execution logic.
 */
class PrintScriptInterpreterTest {

    private static final Position P = new Position(1, 1);

    private final PrintScriptInterpreter interpreter = new PrintScriptInterpreter();

    private static Expression num(double value) {
        return new NumberLiteralExpression(value, P, P);
    }

    private static Expression str(String value) {
        return new StringLiteralExpression(value, P, P);
    }

    private static Expression id(String name) {
        return new IdentifierExpression(name, P, P);
    }

    private static Expression binary(Expression left, BinaryOperator operator, Expression right) {
        return new BinaryExpression(left, operator, right, P, P);
    }

    private static Statement let(String name, String type, Expression initializer) {
        return new VariableDeclarationStatement(name, type, Optional.of(initializer), P, P);
    }

    private static Statement letUninitialized(String name, String type) {
        return new VariableDeclarationStatement(name, type, Optional.empty(), P, P);
    }

    private static Statement assign(String name, Expression value) {
        return new AssignmentStatement(name, value, P, P);
    }

    private static Statement println(Expression argument) {
        return new PrintlnStatement(argument, P, P);
    }

    private String run(Statement... statements) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        interpreter.interpret(List.of(statements).iterator(), new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Test
    void example1_stringConcatenation() {
        // let name: string = "Joe";
        // let lastName: string = "Doe";
        // println(name + " " + lastName);
        String output =
                run(
                        let("name", "string", str("Joe")),
                        let("lastName", "string", str("Doe")),
                        println(binary(binary(id("name"), BinaryOperator.PLUS, str(" ")), BinaryOperator.PLUS, id("lastName"))));

        assertEquals("Joe Doe\n", output);
    }

    @Test
    void example2_numberDivisionInStringConcatenation() {
        // let a: number = 12;
        // let b: number = 4;
        // let c: number = a / b;
        // println("Result: " + c);
        String output =
                run(
                        let("a", "number", num(12)),
                        let("b", "number", num(4)),
                        let("c", "number", binary(id("a"), BinaryOperator.DIVIDE, id("b"))),
                        println(binary(str("Result: "), BinaryOperator.PLUS, id("c"))));

        assertEquals("Result: 3\n", output);
    }

    @Test
    void example3_reassignmentOfADeclaredVariable() {
        // let a: number = 12;
        // let b: number = 4;
        // a = a / b;
        // println("Result: " + a);
        String output =
                run(
                        let("a", "number", num(12)),
                        let("b", "number", num(4)),
                        assign("a", binary(id("a"), BinaryOperator.DIVIDE, id("b"))),
                        println(binary(str("Result: "), BinaryOperator.PLUS, id("a"))));

        assertEquals("Result: 3\n", output);
    }

    @Test
    void arithmeticOperationsOnNumbers() {
        // println(2 + 3 * 4);
        String output = run(println(binary(num(2), BinaryOperator.PLUS, binary(num(3), BinaryOperator.MULTIPLY, num(4)))));

        assertEquals("14\n", output);
    }

    @Test
    void throwsWhenUsingAnUndeclaredVariable() {
        // println(x);
        assertThrows(SemanticException.class, () -> run(println(id("x"))));
    }

    @Test
    void throwsWhenAssigningWrongType() {
        // let x: number = "hello";
        assertThrows(SemanticException.class, () -> run(let("x", "number", str("hello"))));
    }

    @Test
    void throwsWhenUsingVariableBeforeAssignment() {
        // let x: number;
        // println(x);
        assertThrows(SemanticException.class, () -> run(letUninitialized("x", "number"), println(id("x"))));
    }

    @Test
    void executesAnOperatorContributedByAPlugin() {
        // println(7 % 3);
        OperatorDefinition modulo = stubModuloOperator();
        Expression modulo7by3 = new ExtendedBinaryExpression(num(7), modulo, num(3), P, P);

        String output = run(println(modulo7by3));

        assertEquals("1\n", output);
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
