package edu.austral.ingsis.printscript.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SyntaxException;
import edu.austral.ingsis.printscript.common.Version;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BooleanLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link VersionValidator} alone, with the AST built by hand — isolated from whether
 * a real parser would even produce these trees, so it can probe cases (like a boolean literal
 * buried inside a binary expression) a parser-level test wouldn't necessarily exercise.
 */
class VersionValidatorTest {

    private static final Position P = new Position(1, 1);

    private static Expression num(double value) {
        return new NumberLiteralExpression(value, P, P);
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

    private static Statement constDecl(String name, String type, Expression initializer) {
        return new VariableDeclarationStatement(name, type, true, Optional.of(initializer), P, P);
    }

    private static Statement assign(String name, Expression value) {
        return new AssignmentStatement(name, value, P, P);
    }

    private static Statement println(Expression argument) {
        return new PrintlnStatement(argument, P, P);
    }

    /**
     * Pulls every statement through {@code validated}, which is what actually triggers each check -
     * {@link VersionValidator#validating} itself never looks at anything up front.
     */
    private static void drain(Iterator<Statement> validated) {
        validated.forEachRemaining(statement -> {});
    }

    private static void validate(List<Statement> statements, Version version) {
        drain(new VersionValidator(version).validating(statements.iterator()));
    }

    @Test
    void acceptsOnly1_0ConstructsUnder1_0() {
        List<Statement> statements =
                List.of(let("x", "number", num(1)), println(id("x")), assign("x", num(2)));

        assertDoesNotThrow(() -> validate(statements, Version.V1_0));
    }

    @Test
    void throwsOnBooleanLiteralUnder1_0() {
        List<Statement> statements = List.of(println(bool(true)));

        assertThrows(SyntaxException.class, () -> validate(statements, Version.V1_0));
    }

    @Test
    void throwsOnBooleanLiteralNestedInsideABinaryExpressionUnder1_0() {
        // println("value: " + true);  -- the literal isn't the top-level argument
        List<Statement> statements =
                List.of(
                        println(
                                new BinaryExpression(
                                        new StringLiteralExpression("value: ", P, P),
                                        CoreOperators.PLUS,
                                        bool(true),
                                        P,
                                        P)));

        assertThrows(SyntaxException.class, () -> validate(statements, Version.V1_0));
    }

    @Test
    void throwsOnBooleanTypeDeclarationUnder1_0() {
        List<Statement> statements = List.of(let("flag", "boolean", bool(true)));

        assertThrows(SyntaxException.class, () -> validate(statements, Version.V1_0));
    }

    @Test
    void acceptsBooleanConstructsUnder1_1() {
        List<Statement> statements =
                List.of(let("flag", "boolean", bool(true)), println(bool(false)));

        assertDoesNotThrow(() -> validate(statements, Version.V1_1));
    }

    @Test
    void throwsOnConstUnder1_0() {
        List<Statement> statements = List.of(constDecl("x", "number", num(1)));

        assertThrows(SyntaxException.class, () -> validate(statements, Version.V1_0));
    }

    @Test
    void acceptsConstUnder1_1() {
        List<Statement> statements = List.of(constDecl("x", "number", num(1)));

        assertDoesNotThrow(() -> validate(statements, Version.V1_1));
    }

    @Test
    void checksLazilyOnlyWhenPulled() {
        // A violation later in the program doesn't stop an earlier, valid statement from being
        // handed back first - matches how every other error in this language already surfaces
        // (per statement, as it's reached), not as a whole-program check up front.
        List<Statement> statements = List.of(println(id("ok")), println(bool(true)));
        Iterator<Statement> validated =
                new VersionValidator(Version.V1_0).validating(statements.iterator());

        assertDoesNotThrow(validated::next);
        assertThrows(SyntaxException.class, validated::next);
    }
}
