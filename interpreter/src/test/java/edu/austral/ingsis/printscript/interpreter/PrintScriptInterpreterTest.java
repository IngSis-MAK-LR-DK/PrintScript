package edu.austral.ingsis.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.OperatorPrecedence;
import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SemanticException;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BooleanLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.IfStatement;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

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

    private static Expression bool(boolean value) {
        return new BooleanLiteralExpression(value, P, P);
    }

    private static Expression id(String name) {
        return new IdentifierExpression(name, P, P);
    }

    private static Expression binary(
            Expression left, OperatorDefinition operator, Expression right) {
        return new BinaryExpression(left, operator, right, P, P);
    }

    private static Statement let(String name, String type, Expression initializer) {
        return new VariableDeclarationStatement(name, type, Optional.of(initializer), P, P);
    }

    private static Statement letUninitialized(String name, String type) {
        return new VariableDeclarationStatement(name, type, Optional.empty(), P, P);
    }

    private static Statement constDecl(String name, String type, Expression initializer) {
        return new VariableDeclarationStatement(name, type, true, Optional.of(initializer), P, P);
    }

    private static Statement constUninitialized(String name, String type) {
        return new VariableDeclarationStatement(name, type, true, Optional.empty(), P, P);
    }

    private static Statement assign(String name, Expression value) {
        return new AssignmentStatement(name, value, P, P);
    }

    private static Statement println(Expression argument) {
        return new PrintlnStatement(argument, P, P);
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

    private String run(Statement... statements) {
        List<String> lines = new ArrayList<>();
        interpreter.interpret(List.of(statements).iterator(), lines::add);
        return lines.isEmpty() ? "" : String.join("\n", lines) + "\n";
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
                        println(
                                binary(
                                        binary(id("name"), CoreOperators.PLUS, str(" ")),
                                        CoreOperators.PLUS,
                                        id("lastName"))));

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
                        let("c", "number", binary(id("a"), CoreOperators.DIVIDE, id("b"))),
                        println(binary(str("Result: "), CoreOperators.PLUS, id("c"))));

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
                        assign("a", binary(id("a"), CoreOperators.DIVIDE, id("b"))),
                        println(binary(str("Result: "), CoreOperators.PLUS, id("a"))));

        assertEquals("Result: 3\n", output);
    }

    @Test
    void arithmeticOperationsOnNumbers() {
        // println(2 + 3 * 4);
        String output =
                run(
                        println(
                                binary(
                                        num(2),
                                        CoreOperators.PLUS,
                                        binary(num(3), CoreOperators.MULTIPLY, num(4)))));

        assertEquals("14\n", output);
    }

    @Test
    void declaresAndPrintsABooleanVariable() {
        // let isReady: boolean = true;
        // println(isReady);
        String output = run(let("isReady", "boolean", bool(true)), println(id("isReady")));

        assertEquals("true\n", output);
    }

    @Test
    void throwsWhenAssigningANumberToABooleanVariable() {
        // let flag: boolean = 1;
        assertThrows(SemanticException.class, () -> run(let("flag", "boolean", num(1))));
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
        assertThrows(
                SemanticException.class,
                () -> run(letUninitialized("x", "number"), println(id("x"))));
    }

    @Test
    void declaresAndPrintsAConstant() {
        // const x: number = 1;
        // println(x);
        String output = run(constDecl("x", "number", num(1)), println(id("x")));

        assertEquals("1\n", output);
    }

    @Test
    void throwsWhenReassigningAConstant() {
        // const x: number = 1;
        // x = 2;
        assertThrows(
                SemanticException.class,
                () -> run(constDecl("x", "number", num(1)), assign("x", num(2))));
    }

    @Test
    void throwsWhenAConstantHasNoInitializer() {
        // const x: number;
        assertThrows(SemanticException.class, () -> run(constUninitialized("x", "number")));
    }

    @Test
    void ifExecutesThenBranchWhenConditionIsTrue() {
        // let flag: boolean = true;
        // if (flag) { println("yes"); } else { println("no"); }
        String output =
                run(
                        let("flag", "boolean", bool(true)),
                        ifElseStmt(
                                "flag", List.of(println(str("yes"))), List.of(println(str("no")))));

        assertEquals("yes\n", output);
    }

    @Test
    void ifExecutesElseBranchWhenConditionIsFalse() {
        // let flag: boolean = false;
        // if (flag) { println("yes"); } else { println("no"); }
        String output =
                run(
                        let("flag", "boolean", bool(false)),
                        ifElseStmt(
                                "flag", List.of(println(str("yes"))), List.of(println(str("no")))));

        assertEquals("no\n", output);
    }

    @Test
    void ifDoesNothingWhenConditionIsFalseAndThereIsNoElse() {
        // let flag: boolean = false;
        // if (flag) { println("yes"); }
        String output =
                run(let("flag", "boolean", bool(false)), ifStmt("flag", println(str("yes"))));

        assertEquals("", output);
    }

    @Test
    void throwsWhenIfConditionIsNotABooleanVariable() {
        // let flag: number = 1;
        // if (flag) { println("yes"); }
        assertThrows(
                SemanticException.class,
                () -> run(let("flag", "number", num(1)), ifStmt("flag", println(str("yes")))));
    }

    @Test
    void aVariableDeclaredInsideAnIfBlockIsVisibleAfterward() {
        // there's no scoping by block - a declaration inside if leaks to the rest of the program,
        // same environment threads through.
        // let flag: boolean = true;
        // if (flag) { let x: number = 1; }
        // println(x);
        String output =
                run(
                        let("flag", "boolean", bool(true)),
                        ifStmt("flag", let("x", "number", num(1))),
                        println(id("x")));

        assertEquals("1\n", output);
    }

    @Test
    void nestedIfIsEvaluatedCorrectly() {
        // let outer: boolean = true;
        // let inner: boolean = true;
        // if (outer) { if (inner) { println("both"); } }
        String output =
                run(
                        let("outer", "boolean", bool(true)),
                        let("inner", "boolean", bool(true)),
                        ifStmt("outer", ifStmt("inner", println(str("both")))));

        assertEquals("both\n", output);
    }

    @Test
    void executesAnOperatorContributedByAPlugin() {
        // println(7 % 3);
        OperatorDefinition modulo = stubModuloOperator();
        Expression modulo7by3 = new BinaryExpression(num(7), modulo, num(3), P, P);

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
