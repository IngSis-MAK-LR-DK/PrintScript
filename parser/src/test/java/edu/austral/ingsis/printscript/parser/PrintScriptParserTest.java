package edu.austral.ingsis.printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SyntaxException;
import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.TokenStream;
import edu.austral.ingsis.printscript.common.TokenType;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BinaryOperator;
import edu.austral.ingsis.printscript.common.ast.ExtendedBinaryExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the parser alone: token streams are built by hand here instead of going through
 * a real {@code Lexer} - {@code parser} has no dependency (not even a test one) on {@code lexer},
 * so these tests only ever exercise the parser's own grammar logic.
 */
class PrintScriptParserTest {

    private static final Position P = new Position(1, 1);

    private final PrintScriptParser parser = new PrintScriptParser();

    private static Token token(TokenType type, String lexeme) {
        return new Token(type, lexeme, P, P);
    }

    /** Builds a lazy {@link TokenStream} out of literal tokens; the last one must be EOF. */
    private static TokenStream tokenStreamOf(Token... tokens) {
        return buildFrom(Arrays.asList(tokens));
    }

    private static TokenStream buildFrom(List<Token> tokens) {
        Token head = tokens.get(0);
        if (head.type() == TokenType.EOF) {
            return new TokenStream(head, () -> buildFrom(tokens)); // never invoked, tail() short-circuits at EOF
        }
        List<Token> rest = tokens.subList(1, tokens.size());
        return new TokenStream(head, () -> buildFrom(rest));
    }

    private static final Token EOF = token(TokenType.EOF, "");

    private List<Statement> parse(Token... tokens) {
        List<Statement> statements = new ArrayList<>();
        parser.parse(tokenStreamOf(tokens)).forEachRemaining(statements::add);
        return statements;
    }

    @Test
    void parsesVariableDeclarationWithInitializer() {
        // let x: number = 12;
        List<Statement> statements =
                parse(
                        token(TokenType.LET, "let"),
                        token(TokenType.IDENTIFIER, "x"),
                        token(TokenType.COLON, ":"),
                        token(TokenType.IDENTIFIER, "number"),
                        token(TokenType.EQUALS, "="),
                        token(TokenType.NUMBER_LITERAL, "12"),
                        token(TokenType.SEMICOLON, ";"),
                        EOF);

        assertEquals(1, statements.size());
        var declaration = assertInstanceOf(VariableDeclarationStatement.class, statements.get(0));
        assertEquals("x", declaration.identifierName());
        assertEquals("number", declaration.typeName());
        assertTrue(declaration.initializer().isPresent());
    }

    @Test
    void parsesVariableDeclarationWithoutInitializer() {
        // let x: string;
        List<Statement> statements =
                parse(
                        token(TokenType.LET, "let"),
                        token(TokenType.IDENTIFIER, "x"),
                        token(TokenType.COLON, ":"),
                        token(TokenType.IDENTIFIER, "string"),
                        token(TokenType.SEMICOLON, ";"),
                        EOF);

        var declaration = assertInstanceOf(VariableDeclarationStatement.class, statements.get(0));
        assertTrue(declaration.initializer().isEmpty());
    }

    @Test
    void parsesAssignment() {
        // x = 5;
        List<Statement> statements =
                parse(
                        token(TokenType.IDENTIFIER, "x"),
                        token(TokenType.EQUALS, "="),
                        token(TokenType.NUMBER_LITERAL, "5"),
                        token(TokenType.SEMICOLON, ";"),
                        EOF);

        var assignment = assertInstanceOf(AssignmentStatement.class, statements.get(0));
        assertEquals("x", assignment.identifierName());
    }

    @Test
    void parsesPrintlnWithConcatenation() {
        // println("Result: " + a);
        List<Statement> statements =
                parse(
                        token(TokenType.PRINTLN, "println"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.STRING_LITERAL, "Result: "),
                        token(TokenType.PLUS, "+"),
                        token(TokenType.IDENTIFIER, "a"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.SEMICOLON, ";"),
                        EOF);

        var println = assertInstanceOf(PrintlnStatement.class, statements.get(0));
        var concatenation = assertInstanceOf(BinaryExpression.class, println.argument());
        assertEquals(BinaryOperator.PLUS, concatenation.operator());
        assertInstanceOf(StringLiteralExpression.class, concatenation.left());
    }

    @Test
    void respectsOperatorPrecedence() {
        // x = 2 + 3 * 4; must parse as 2 + (3 * 4)
        List<Statement> statements =
                parse(
                        token(TokenType.IDENTIFIER, "x"),
                        token(TokenType.EQUALS, "="),
                        token(TokenType.NUMBER_LITERAL, "2"),
                        token(TokenType.PLUS, "+"),
                        token(TokenType.NUMBER_LITERAL, "3"),
                        token(TokenType.STAR, "*"),
                        token(TokenType.NUMBER_LITERAL, "4"),
                        token(TokenType.SEMICOLON, ";"),
                        EOF);

        var assignment = assertInstanceOf(AssignmentStatement.class, statements.get(0));
        var addition = assertInstanceOf(BinaryExpression.class, assignment.value());
        assertEquals(BinaryOperator.PLUS, addition.operator());
        assertInstanceOf(BinaryExpression.class, addition.right());
    }

    @Test
    void parsesMultipleStatementsLazily() {
        // let a: number = 1;
        // let b: number = 2;
        // println(a + b);
        List<Statement> statements =
                parse(
                        token(TokenType.LET, "let"),
                        token(TokenType.IDENTIFIER, "a"),
                        token(TokenType.COLON, ":"),
                        token(TokenType.IDENTIFIER, "number"),
                        token(TokenType.EQUALS, "="),
                        token(TokenType.NUMBER_LITERAL, "1"),
                        token(TokenType.SEMICOLON, ";"),
                        token(TokenType.LET, "let"),
                        token(TokenType.IDENTIFIER, "b"),
                        token(TokenType.COLON, ":"),
                        token(TokenType.IDENTIFIER, "number"),
                        token(TokenType.EQUALS, "="),
                        token(TokenType.NUMBER_LITERAL, "2"),
                        token(TokenType.SEMICOLON, ";"),
                        token(TokenType.PRINTLN, "println"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.IDENTIFIER, "a"),
                        token(TokenType.PLUS, "+"),
                        token(TokenType.IDENTIFIER, "b"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.SEMICOLON, ";"),
                        EOF);

        assertEquals(3, statements.size());
    }

    @Test
    void throwsOnMissingSemicolon() {
        // let x: number = 1  (no closing semicolon)
        assertThrows(
                SyntaxException.class,
                () ->
                        parse(
                                token(TokenType.LET, "let"),
                                token(TokenType.IDENTIFIER, "x"),
                                token(TokenType.COLON, ":"),
                                token(TokenType.IDENTIFIER, "number"),
                                token(TokenType.EQUALS, "="),
                                token(TokenType.NUMBER_LITERAL, "1"),
                                EOF));
    }

    @Test
    void throwsOnMissingColon() {
        // let x number = 1;  (no colon)
        assertThrows(
                SyntaxException.class,
                () ->
                        parse(
                                token(TokenType.LET, "let"),
                                token(TokenType.IDENTIFIER, "x"),
                                token(TokenType.IDENTIFIER, "number"),
                                token(TokenType.EQUALS, "="),
                                token(TokenType.NUMBER_LITERAL, "1"),
                                token(TokenType.SEMICOLON, ";"),
                                EOF));
    }

    @Test
    void parsesExtensionOperatorWhenRegistered() {
        // x = 7 % 3;
        OperatorDefinition modulo = stubModuloOperator();
        PrintScriptParser pluggableParser = new PrintScriptParser(Set.of(modulo));

        TokenStream tokens =
                tokenStreamOf(
                        token(TokenType.IDENTIFIER, "x"),
                        token(TokenType.EQUALS, "="),
                        token(TokenType.NUMBER_LITERAL, "7"),
                        token(TokenType.EXTENSION_OPERATOR, "%"),
                        token(TokenType.NUMBER_LITERAL, "3"),
                        token(TokenType.SEMICOLON, ";"),
                        EOF);

        List<Statement> statements = new ArrayList<>();
        pluggableParser.parse(tokens).forEachRemaining(statements::add);

        var assignment = assertInstanceOf(AssignmentStatement.class, statements.get(0));
        var extended = assertInstanceOf(ExtendedBinaryExpression.class, assignment.value());
        assertEquals(modulo, extended.operator());
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
