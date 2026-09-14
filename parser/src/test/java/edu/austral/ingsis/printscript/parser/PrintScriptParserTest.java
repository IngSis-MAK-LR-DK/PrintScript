package edu.austral.ingsis.printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.OperatorPrecedence;
import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SyntaxException;
import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.TokenStream;
import edu.austral.ingsis.printscript.common.TokenType;
import edu.austral.ingsis.printscript.common.Version;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BooleanLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.IfStatement;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.ReadEnvExpression;
import edu.austral.ingsis.printscript.common.ast.ReadInputExpression;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the parser alone: token streams are built by hand here instead of going through a
 * real {@code Lexer} - {@code parser} has no dependency (not even a test one) on {@code lexer}, so
 * these tests only ever exercise the parser's own grammar logic.
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
            return new TokenStream(
                    head, () -> buildFrom(tokens)); // never invoked, tail() short-circuits at EOF
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
                        token(TokenType.OPERATOR, "+"),
                        token(TokenType.IDENTIFIER, "a"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.SEMICOLON, ";"),
                        EOF);

        var println = assertInstanceOf(PrintlnStatement.class, statements.get(0));
        var concatenation = assertInstanceOf(BinaryExpression.class, println.argument());
        assertEquals(CoreOperators.PLUS, concatenation.operator());
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
                        token(TokenType.OPERATOR, "+"),
                        token(TokenType.NUMBER_LITERAL, "3"),
                        token(TokenType.OPERATOR, "*"),
                        token(TokenType.NUMBER_LITERAL, "4"),
                        token(TokenType.SEMICOLON, ";"),
                        EOF);

        var assignment = assertInstanceOf(AssignmentStatement.class, statements.get(0));
        var addition = assertInstanceOf(BinaryExpression.class, assignment.value());
        assertEquals(CoreOperators.PLUS, addition.operator());
        assertInstanceOf(BinaryExpression.class, addition.right());
    }

    @Test
    void parsesMultipleStatements() {
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
                        token(TokenType.OPERATOR, "+"),
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
    void parsesBooleanLiteralUnder1_1() {
        // let flag: boolean = true;
        PrintScriptParser v1_1Parser = new PrintScriptParser(Set.of(), Version.V1_1);
        List<Statement> statements = new ArrayList<>();
        v1_1Parser
                .parse(
                        tokenStreamOf(
                                token(TokenType.LET, "let"),
                                token(TokenType.IDENTIFIER, "flag"),
                                token(TokenType.COLON, ":"),
                                token(TokenType.IDENTIFIER, "boolean"),
                                token(TokenType.EQUALS, "="),
                                token(TokenType.BOOLEAN_LITERAL, "true"),
                                token(TokenType.SEMICOLON, ";"),
                                EOF))
                .forEachRemaining(statements::add);

        var declaration = assertInstanceOf(VariableDeclarationStatement.class, statements.get(0));
        assertEquals("boolean", declaration.typeName());
        var literal =
                assertInstanceOf(BooleanLiteralExpression.class, declaration.initializer().get());
        assertTrue(literal.value());
    }

    @Test
    void throwsOnBooleanLiteralUnder1_0() {
        // println(true);  -- parser defaults to 1.0
        assertThrows(
                SyntaxException.class,
                () ->
                        parse(
                                token(TokenType.PRINTLN, "println"),
                                token(TokenType.LEFT_PAREN, "("),
                                token(TokenType.BOOLEAN_LITERAL, "true"),
                                token(TokenType.RIGHT_PAREN, ")"),
                                token(TokenType.SEMICOLON, ";"),
                                EOF));
    }

    @Test
    void throwsOnBooleanTypeUnder1_0() {
        // let flag: boolean;  -- parser defaults to 1.0
        assertThrows(
                SyntaxException.class,
                () ->
                        parse(
                                token(TokenType.LET, "let"),
                                token(TokenType.IDENTIFIER, "flag"),
                                token(TokenType.COLON, ":"),
                                token(TokenType.IDENTIFIER, "boolean"),
                                token(TokenType.SEMICOLON, ";"),
                                EOF));
    }

    @Test
    void parsesConstDeclarationUnder1_1() {
        // const x: number = 1;
        PrintScriptParser v1_1Parser = new PrintScriptParser(Set.of(), Version.V1_1);
        List<Statement> statements = new ArrayList<>();
        v1_1Parser
                .parse(
                        tokenStreamOf(
                                token(TokenType.CONST, "const"),
                                token(TokenType.IDENTIFIER, "x"),
                                token(TokenType.COLON, ":"),
                                token(TokenType.IDENTIFIER, "number"),
                                token(TokenType.EQUALS, "="),
                                token(TokenType.NUMBER_LITERAL, "1"),
                                token(TokenType.SEMICOLON, ";"),
                                EOF))
                .forEachRemaining(statements::add);

        var declaration = assertInstanceOf(VariableDeclarationStatement.class, statements.get(0));
        assertTrue(declaration.isConstant());
    }

    @Test
    void letDeclarationIsNotConstant() {
        // let x: number = 1;
        List<Statement> statements =
                parse(
                        token(TokenType.LET, "let"),
                        token(TokenType.IDENTIFIER, "x"),
                        token(TokenType.COLON, ":"),
                        token(TokenType.IDENTIFIER, "number"),
                        token(TokenType.EQUALS, "="),
                        token(TokenType.NUMBER_LITERAL, "1"),
                        token(TokenType.SEMICOLON, ";"),
                        EOF);

        var declaration = assertInstanceOf(VariableDeclarationStatement.class, statements.get(0));
        assertTrue(!declaration.isConstant());
    }

    @Test
    void throwsOnConstUnder1_0() {
        // const x: number = 1;  -- parser defaults to 1.0
        assertThrows(
                SyntaxException.class,
                () ->
                        parse(
                                token(TokenType.CONST, "const"),
                                token(TokenType.IDENTIFIER, "x"),
                                token(TokenType.COLON, ":"),
                                token(TokenType.IDENTIFIER, "number"),
                                token(TokenType.EQUALS, "="),
                                token(TokenType.NUMBER_LITERAL, "1"),
                                token(TokenType.SEMICOLON, ";"),
                                EOF));
    }

    @Test
    void parsesIfWithoutElse() {
        // if (flag) { println(x); }
        PrintScriptParser v1_1Parser = new PrintScriptParser(Set.of(), Version.V1_1);
        List<Statement> statements = new ArrayList<>();
        v1_1Parser
                .parse(
                        tokenStreamOf(
                                token(TokenType.IF, "if"),
                                token(TokenType.LEFT_PAREN, "("),
                                token(TokenType.IDENTIFIER, "flag"),
                                token(TokenType.RIGHT_PAREN, ")"),
                                token(TokenType.LEFT_BRACE, "{"),
                                token(TokenType.PRINTLN, "println"),
                                token(TokenType.LEFT_PAREN, "("),
                                token(TokenType.IDENTIFIER, "x"),
                                token(TokenType.RIGHT_PAREN, ")"),
                                token(TokenType.SEMICOLON, ";"),
                                token(TokenType.RIGHT_BRACE, "}"),
                                EOF))
                .forEachRemaining(statements::add);

        var ifStatement = assertInstanceOf(IfStatement.class, statements.get(0));
        assertEquals("flag", ifStatement.condition().name());
        assertEquals(1, ifStatement.thenBranch().size());
        assertTrue(ifStatement.elseBranch().isEmpty());
    }

    @Test
    void parsesIfWithElse() {
        // if (flag) { } else { }
        PrintScriptParser v1_1Parser = new PrintScriptParser(Set.of(), Version.V1_1);
        List<Statement> statements = new ArrayList<>();
        v1_1Parser
                .parse(
                        tokenStreamOf(
                                token(TokenType.IF, "if"),
                                token(TokenType.LEFT_PAREN, "("),
                                token(TokenType.IDENTIFIER, "flag"),
                                token(TokenType.RIGHT_PAREN, ")"),
                                token(TokenType.LEFT_BRACE, "{"),
                                token(TokenType.RIGHT_BRACE, "}"),
                                token(TokenType.ELSE, "else"),
                                token(TokenType.LEFT_BRACE, "{"),
                                token(TokenType.RIGHT_BRACE, "}"),
                                EOF))
                .forEachRemaining(statements::add);

        var ifStatement = assertInstanceOf(IfStatement.class, statements.get(0));
        assertTrue(ifStatement.elseBranch().isPresent());
        assertTrue(ifStatement.thenBranch().isEmpty());
        assertTrue(ifStatement.elseBranch().get().isEmpty());
    }

    @Test
    void parsesNestedIfInsideABlock() {
        // if (outer) { if (inner) { } }
        PrintScriptParser v1_1Parser = new PrintScriptParser(Set.of(), Version.V1_1);
        List<Statement> statements = new ArrayList<>();
        v1_1Parser
                .parse(
                        tokenStreamOf(
                                token(TokenType.IF, "if"),
                                token(TokenType.LEFT_PAREN, "("),
                                token(TokenType.IDENTIFIER, "outer"),
                                token(TokenType.RIGHT_PAREN, ")"),
                                token(TokenType.LEFT_BRACE, "{"),
                                token(TokenType.IF, "if"),
                                token(TokenType.LEFT_PAREN, "("),
                                token(TokenType.IDENTIFIER, "inner"),
                                token(TokenType.RIGHT_PAREN, ")"),
                                token(TokenType.LEFT_BRACE, "{"),
                                token(TokenType.RIGHT_BRACE, "}"),
                                token(TokenType.RIGHT_BRACE, "}"),
                                EOF))
                .forEachRemaining(statements::add);

        var outer = assertInstanceOf(IfStatement.class, statements.get(0));
        assertEquals(1, outer.thenBranch().size());
        assertInstanceOf(IfStatement.class, outer.thenBranch().get(0));
    }

    @Test
    void throwsWhenIfConditionIsNotAnIdentifier() {
        // if (1) { }  -- condition must be a bare variable name
        PrintScriptParser v1_1Parser = new PrintScriptParser(Set.of(), Version.V1_1);
        assertThrows(
                SyntaxException.class,
                () ->
                        v1_1Parser
                                .parse(
                                        tokenStreamOf(
                                                token(TokenType.IF, "if"),
                                                token(TokenType.LEFT_PAREN, "("),
                                                token(TokenType.NUMBER_LITERAL, "1"),
                                                token(TokenType.RIGHT_PAREN, ")"),
                                                token(TokenType.LEFT_BRACE, "{"),
                                                token(TokenType.RIGHT_BRACE, "}"),
                                                EOF))
                                .forEachRemaining(s -> {}));
    }

    @Test
    void throwsWhenIfBlockIsMissingClosingBrace() {
        // if (flag) {  -- no closing '}'
        PrintScriptParser v1_1Parser = new PrintScriptParser(Set.of(), Version.V1_1);
        assertThrows(
                SyntaxException.class,
                () ->
                        v1_1Parser
                                .parse(
                                        tokenStreamOf(
                                                token(TokenType.IF, "if"),
                                                token(TokenType.LEFT_PAREN, "("),
                                                token(TokenType.IDENTIFIER, "flag"),
                                                token(TokenType.RIGHT_PAREN, ")"),
                                                token(TokenType.LEFT_BRACE, "{"),
                                                EOF))
                                .forEachRemaining(s -> {}));
    }

    @Test
    void throwsOnIfUnder1_0() {
        // if (flag) { }  -- parser defaults to 1.0
        assertThrows(
                SyntaxException.class,
                () ->
                        parse(
                                token(TokenType.IF, "if"),
                                token(TokenType.LEFT_PAREN, "("),
                                token(TokenType.IDENTIFIER, "flag"),
                                token(TokenType.RIGHT_PAREN, ")"),
                                token(TokenType.LEFT_BRACE, "{"),
                                token(TokenType.RIGHT_BRACE, "}"),
                                EOF));
    }

    @Test
    void parsesReadInputUnder1_1() {
        // let name: string = readInput("Your name:");
        PrintScriptParser v1_1Parser = new PrintScriptParser(Set.of(), Version.V1_1);
        List<Statement> statements = new ArrayList<>();
        v1_1Parser
                .parse(
                        tokenStreamOf(
                                token(TokenType.LET, "let"),
                                token(TokenType.IDENTIFIER, "name"),
                                token(TokenType.COLON, ":"),
                                token(TokenType.IDENTIFIER, "string"),
                                token(TokenType.EQUALS, "="),
                                token(TokenType.READ_INPUT, "readInput"),
                                token(TokenType.LEFT_PAREN, "("),
                                token(TokenType.STRING_LITERAL, "Your name:"),
                                token(TokenType.RIGHT_PAREN, ")"),
                                token(TokenType.SEMICOLON, ";"),
                                EOF))
                .forEachRemaining(statements::add);

        var declaration = assertInstanceOf(VariableDeclarationStatement.class, statements.get(0));
        var readInput =
                assertInstanceOf(ReadInputExpression.class, declaration.initializer().get());
        assertInstanceOf(StringLiteralExpression.class, readInput.message());
    }

    @Test
    void parsesReadEnvUnder1_1() {
        // let port: number = readEnv("PORT");
        PrintScriptParser v1_1Parser = new PrintScriptParser(Set.of(), Version.V1_1);
        List<Statement> statements = new ArrayList<>();
        v1_1Parser
                .parse(
                        tokenStreamOf(
                                token(TokenType.LET, "let"),
                                token(TokenType.IDENTIFIER, "port"),
                                token(TokenType.COLON, ":"),
                                token(TokenType.IDENTIFIER, "number"),
                                token(TokenType.EQUALS, "="),
                                token(TokenType.READ_ENV, "readEnv"),
                                token(TokenType.LEFT_PAREN, "("),
                                token(TokenType.STRING_LITERAL, "PORT"),
                                token(TokenType.RIGHT_PAREN, ")"),
                                token(TokenType.SEMICOLON, ";"),
                                EOF))
                .forEachRemaining(statements::add);

        var declaration = assertInstanceOf(VariableDeclarationStatement.class, statements.get(0));
        var readEnv = assertInstanceOf(ReadEnvExpression.class, declaration.initializer().get());
        assertInstanceOf(StringLiteralExpression.class, readEnv.variableName());
    }

    @Test
    void throwsOnReadInputUnder1_0() {
        // println(readInput("x"));  -- parser defaults to 1.0
        assertThrows(
                SyntaxException.class,
                () ->
                        parse(
                                token(TokenType.PRINTLN, "println"),
                                token(TokenType.LEFT_PAREN, "("),
                                token(TokenType.READ_INPUT, "readInput"),
                                token(TokenType.LEFT_PAREN, "("),
                                token(TokenType.STRING_LITERAL, "x"),
                                token(TokenType.RIGHT_PAREN, ")"),
                                token(TokenType.RIGHT_PAREN, ")"),
                                token(TokenType.SEMICOLON, ";"),
                                EOF));
    }

    @Test
    void throwsOnReadEnvUnder1_0() {
        // println(readEnv("x"));  -- parser defaults to 1.0
        assertThrows(
                SyntaxException.class,
                () ->
                        parse(
                                token(TokenType.PRINTLN, "println"),
                                token(TokenType.LEFT_PAREN, "("),
                                token(TokenType.READ_ENV, "readEnv"),
                                token(TokenType.LEFT_PAREN, "("),
                                token(TokenType.STRING_LITERAL, "x"),
                                token(TokenType.RIGHT_PAREN, ")"),
                                token(TokenType.RIGHT_PAREN, ")"),
                                token(TokenType.SEMICOLON, ";"),
                                EOF));
    }

    @Test
    void parsesExtensionOperatorWhenRegistered() {
        // x = 7 % 3;
        OperatorDefinition modulo = stubOperator("%");
        PrintScriptParser pluggableParser = new PrintScriptParser(Set.of(modulo));

        TokenStream tokens =
                tokenStreamOf(
                        token(TokenType.IDENTIFIER, "x"),
                        token(TokenType.EQUALS, "="),
                        token(TokenType.NUMBER_LITERAL, "7"),
                        token(TokenType.OPERATOR, "%"),
                        token(TokenType.NUMBER_LITERAL, "3"),
                        token(TokenType.SEMICOLON, ";"),
                        EOF);

        List<Statement> statements = new ArrayList<>();
        pluggableParser.parse(tokens).forEachRemaining(statements::add);

        var assignment = assertInstanceOf(AssignmentStatement.class, statements.get(0));
        var binary = assertInstanceOf(BinaryExpression.class, assignment.value());
        assertEquals(modulo, binary.operator());
    }

    @Test
    void throwsWhenTwoOperatorPluginsRegisterTheSameSymbol() {
        OperatorDefinition first = stubOperator("%");
        OperatorDefinition second = stubOperator("%");

        assertThrows(
                IllegalArgumentException.class, () -> new PrintScriptParser(Set.of(first, second)));
    }

    private static OperatorDefinition stubOperator(String symbol) {
        return new OperatorDefinition() {
            @Override
            public String symbol() {
                return symbol;
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
