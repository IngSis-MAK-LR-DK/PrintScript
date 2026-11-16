package edu.austral.ingsis.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SemanticException;
import edu.austral.ingsis.printscript.common.SourceRange;
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

import org.junit.jupiter.api.Test;

class PrintScriptSemanticAnalyzerTest {

    private static final Position P = new Position(1, 1);

    private final SemanticAnalyzer analyzer = new PrintScriptSemanticAnalyzer();

    private static Expression num(double value) {
        return new NumberLiteralExpression(value, P, P);
    }

    private static Expression str(String value) {
        return new StringLiteralExpression(value, P, P);
    }

    private static Expression bool(boolean value) {
        return new BooleanLiteralExpression(value, P, P);
    }

    private static IdentifierExpression id(String name) {
        return new IdentifierExpression(name, P, P);
    }

    private static Expression binary(Expression left, OperatorDefinition op, Expression right) {
        return new BinaryExpression(left, op, right, P, P);
    }

    private static Expression readInput() {
        return new ReadInputExpression(str("?"), P, P);
    }

    private static Expression readEnv() {
        return new ReadEnvExpression(str("HOME"), P, P);
    }

    private static Statement let(String name, String type, Expression initializer) {
        return new VariableDeclarationStatement(name, type, Optional.of(initializer), P, P);
    }

    private static Statement let(String name, String type) {
        return new VariableDeclarationStatement(name, type, Optional.empty(), P, P);
    }

    private static Statement constant(String name, String type, Expression initializer) {
        return new VariableDeclarationStatement(name, type, true, Optional.of(initializer), P, P);
    }

    private static Statement assign(String name, Expression value) {
        return new AssignmentStatement(name, value, P, P);
    }

    private static Statement println(Expression argument) {
        return new PrintlnStatement(argument, P, P);
    }

    private static Statement ifElse(String condition, List<Statement> then, List<Statement> other) {
        return new IfStatement(id(condition), then, Optional.of(other), P, P);
    }

    private static Statement ifOnly(String condition, Statement... then) {
        return new IfStatement(id(condition), List.of(then), Optional.empty(), P, P);
    }

    private void assertValid(Statement... statements) {
        assertDoesNotThrow(() -> analyzer.analyze(List.of(statements).iterator()));
    }

    private void assertRejected(String expectedMessagePart, Statement... statements) {
        SemanticException e =
                assertThrows(
                        SemanticException.class,
                        () -> analyzer.analyze(List.of(statements).iterator()));
        assertTrue(e.getMessage().contains(expectedMessagePart), e.getMessage());
    }

    @Test
    void acceptsAWellTypedProgram() {
        assertValid(
                let("a", "number", num(1)),
                let("s", "string", binary(str("x"), CoreOperators.PLUS, id("a"))),
                assign("a", binary(id("a"), CoreOperators.MULTIPLY, num(2))),
                println(id("s")));
    }

    @Test
    void rejectsAnUndeclaredVariable() {
        assertRejected("is not declared", println(id("nope")));
        assertRejected("is not declared", assign("nope", num(1)));
    }

    @Test
    void rejectsAVariableUsedBeforeBeingAssigned() {
        assertRejected("used before being assigned", let("a", "number"), println(id("a")));
    }

    @Test
    void rejectsAValueOfTheWrongType() {
        assertRejected("Cannot assign", let("a", "number", str("x")));
        assertRejected("Cannot assign", let("a", "number"), assign("a", str("x")));
    }

    @Test
    void rejectsArithmeticOnNonNumbers() {
        assertRejected(
                "requires operands of type number",
                println(binary(str("x"), CoreOperators.MULTIPLY, num(2))));
        assertRejected(
                "requires operands of type number",
                println(binary(bool(true), CoreOperators.PLUS, num(2))));
    }

    @Test
    void rejectsARedeclarationPointingAtTheName() {
        SourceRange name = new SourceRange(new Position(2, 5), new Position(2, 6));
        SourceRange type = new SourceRange(new Position(2, 8), new Position(2, 14));
        Statement redeclaration =
                new VariableDeclarationStatement(
                        "a", "number", false, Optional.empty(), name, type, P, P);

        SemanticException e =
                assertThrows(
                        SemanticException.class,
                        () ->
                                analyzer.analyze(
                                        List.of(let("a", "number"), redeclaration).iterator()));

        assertEquals(name.start(), e.start());
        assertEquals(name.end(), e.end());
    }

    @Test
    void rejectsAnUnknownTypePointingAtTheType() {
        SourceRange name = new SourceRange(new Position(1, 5), new Position(1, 6));
        SourceRange type = new SourceRange(new Position(1, 8), new Position(1, 11));
        Statement declaration =
                new VariableDeclarationStatement(
                        "a", "foo", false, Optional.empty(), name, type, P, P);

        SemanticException e =
                assertThrows(
                        SemanticException.class,
                        () -> analyzer.analyze(List.of(declaration).iterator()));

        assertEquals(type.start(), e.start());
        assertEquals(type.end(), e.end());
    }

    @Test
    void rejectsAConstantWithoutInitializerOrReassigned() {
        assertRejected(
                "must be initialized",
                new VariableDeclarationStatement("c", "number", true, Optional.empty(), P, P));
        assertRejected("Cannot reassign", constant("c", "number", num(1)), assign("c", num(2)));
    }

    @Test
    void acceptsExternalValuesForAnyDeclaredTypeWithoutReadingThem() {
        assertValid(let("n", "number", readInput()), let("b", "boolean", readEnv()));
    }

    @Test
    void treatsAnExternalValueInsideAnOperationAsAString() {
        assertRejected(
                "Cannot assign",
                let("n", "number", binary(readInput(), CoreOperators.PLUS, num(1))));
    }

    @Test
    void checksTheArgumentOfExternalReads() {
        assertRejected(
                "is not declared", let("n", "string", new ReadInputExpression(id("nope"), P, P)));
        assertRejected(
                "is not declared", let("n", "string", new ReadEnvExpression(id("nope"), P, P)));
    }

    @Test
    void checksBothBranchesOfAnIfWhicheverWouldRun() {
        Statement condition = constant("c", "boolean", bool(false));

        assertRejected("is not declared", condition, ifOnly("c", println(id("nope"))));
        assertRejected(
                "is not declared",
                constant("c", "boolean", bool(true)),
                ifElse("c", List.of(), List.of(println(id("nope")))));
    }

    @Test
    void rejectsAnIfConditionThatIsNotABooleanVariable() {
        assertRejected("must be a 'boolean'", let("n", "number", num(1)), ifOnly("n"));
        assertRejected("is not declared", ifOnly("nope"));
    }

    @Test
    void acceptsAVariableThatOnlyOneBranchAssigns() {
        assertValid(
                let("c", "boolean", bool(true)),
                let("x", "number"),
                ifOnly("c", assign("x", num(1))),
                println(id("x")));
    }

    @Test
    void acceptsAVariableThatOnlyOneBranchDeclares() {
        assertValid(
                let("c", "boolean", bool(true)),
                ifOnly("c", let("y", "number", num(1))),
                println(id("y")));
    }

    @Test
    void acceptsRedeclaringWhatOnlyOneBranchDeclared() {
        assertValid(
                let("c", "boolean", bool(true)),
                ifOnly("c", let("y", "number", num(1))),
                let("y", "string", str("x")));
    }

    @Test
    void rejectsRedeclaringWhatBothBranchesDeclared() {
        assertRejected(
                "is already declared",
                let("c", "boolean", bool(true)),
                ifElse(
                        "c",
                        List.of(let("y", "number", num(1))),
                        List.of(let("y", "number", num(2)))),
                let("y", "number", num(3)));
    }

    @Test
    void doesNotRejectAVariableWhoseBranchesGaveItDifferentTypes() {
        assertValid(
                let("c", "boolean", bool(true)),
                ifElse(
                        "c",
                        List.of(let("y", "number", num(1))),
                        List.of(let("y", "string", str("x")))),
                println(binary(id("y"), CoreOperators.MINUS, num(1))),
                assign("y", bool(true)),
                let("z", "number", id("y")));
    }

    @Test
    void acceptsABooleanConditionWhoseBranchesGaveItDifferentTypes() {
        assertValid(
                let("c", "boolean", bool(true)),
                ifElse(
                        "c",
                        List.of(let("y", "number", num(1))),
                        List.of(let("y", "boolean", bool(true)))),
                ifOnly("y"));
    }
}
