package edu.austral.ingsis.printscript.parser;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.SyntaxException;
import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.TokenStream;
import edu.austral.ingsis.printscript.common.TokenType;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

/**
 * Recursive-descent parser for the PrintScript 1.0 grammar:
 *
 * <pre>
 * statement   := declaration | assignment | println
 * declaration := "let" IDENTIFIER ":" IDENTIFIER ("=" expression)? ";"
 * assignment  := IDENTIFIER "=" expression ";"
 * println     := "println" "(" expression ")" ";"
 * expression  := primary (OPERATOR primary)*
 * primary     := NUMBER | STRING | IDENTIFIER | "(" expression ")"
 * </pre>
 *
 * {@code expression} isn't split into separate grammar levels for each precedence —
 * an operator's precedence comes from the operator table and is applied dynamically in
 * {@link #parseExpression(TokenStream, int)}, so a new operator changes only that table,
 * never this grammar.
 * <p>
 * Every {@code parseX} method here is pure: it takes the {@link TokenStream} to read from and
 * returns a {@link ParseResult} with what it built and the stream that's left over — nothing is
 * mutated along the way. {@link #tokens} is the only mutable state in this class, and it only
 * changes once per call to {@link #next()}, to remember where the previous statement left off.
 */
final class StatementIterator implements Iterator<Statement> {

    private TokenStream tokens;
    private final Map<String, OperatorDefinition> operators;

    StatementIterator(TokenStream tokens, Map<String, OperatorDefinition> operators) {
        this.tokens = tokens;
        this.operators = operators;
    }

    @Override
    public boolean hasNext() {
        return !check(tokens, TokenType.EOF);
    }

    @Override
    public Statement next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more statements");
        }
        ParseResult<Statement> result = parseStatement(tokens);
        tokens = result.rest();
        return result.node();
    }

    private ParseResult<Statement> parseStatement(TokenStream tokens) {
        Token current = peek(tokens);
        return switch (current.type()) {
            case LET -> parseVariableDeclaration(tokens);
            case PRINTLN -> parsePrintln(tokens);
            case IDENTIFIER -> parseAssignment(tokens);
            default ->
                    throw new SyntaxException(
                            "Expected a statement but found '" + current.lexeme() + "'",
                            current.start(),
                            current.end());
        };
    }

    private ParseResult<Statement> parseVariableDeclaration(TokenStream tokens) {
        ParseResult<Token> let = expect(tokens, TokenType.LET, "Expected 'let'");
        ParseResult<Token> name =
                expect(let.rest(), TokenType.IDENTIFIER, "Expected a variable name");
        ParseResult<Token> colon =
                expect(name.rest(), TokenType.COLON, "Expected ':' after variable name");
        ParseResult<Token> type =
                expect(colon.rest(), TokenType.IDENTIFIER, "Expected a type name");

        TokenStream rest = type.rest();
        Optional<Expression> initializer = Optional.empty();
        if (check(rest, TokenType.EQUALS)) {
            ParseResult<Token> equals = advance(rest);
            ParseResult<Expression> value = parseExpression(equals.rest());
            initializer = Optional.of(value.node());
            rest = value.rest();
        }

        ParseResult<Token> semicolon =
                expect(rest, TokenType.SEMICOLON, "Expected ';' after declaration");
        Statement declaration =
                new VariableDeclarationStatement(
                        name.node().lexeme(),
                        type.node().lexeme(),
                        initializer,
                        let.node().start(),
                        semicolon.node().end());
        return new ParseResult<>(declaration, semicolon.rest());
    }

    private ParseResult<Statement> parseAssignment(TokenStream tokens) {
        ParseResult<Token> name = expect(tokens, TokenType.IDENTIFIER, "Expected a variable name");
        ParseResult<Token> equals =
                expect(name.rest(), TokenType.EQUALS, "Expected '=' after identifier");
        ParseResult<Expression> value = parseExpression(equals.rest());
        ParseResult<Token> semicolon =
                expect(value.rest(), TokenType.SEMICOLON, "Expected ';' after assignment");
        Statement assignment =
                new AssignmentStatement(
                        name.node().lexeme(),
                        value.node(),
                        name.node().start(),
                        semicolon.node().end());
        return new ParseResult<>(assignment, semicolon.rest());
    }

    private ParseResult<Statement> parsePrintln(TokenStream tokens) {
        ParseResult<Token> println = expect(tokens, TokenType.PRINTLN, "Expected 'println'");
        ParseResult<Token> leftParen =
                expect(println.rest(), TokenType.LEFT_PAREN, "Expected '(' after 'println'");
        ParseResult<Expression> argument = parseExpression(leftParen.rest());
        ParseResult<Token> rightParen =
                expect(
                        argument.rest(),
                        TokenType.RIGHT_PAREN,
                        "Expected ')' after println argument");
        ParseResult<Token> semicolon =
                expect(rightParen.rest(), TokenType.SEMICOLON, "Expected ';' after println call");
        Statement statement =
                new PrintlnStatement(
                        argument.node(), println.node().start(), semicolon.node().end());
        return new ParseResult<>(statement, semicolon.rest());
    }

    private ParseResult<Expression> parseExpression(TokenStream tokens) {
        return parseExpression(tokens, 0);
    }

    /**
     * Precedence climbing: consumes operators whose precedence is at least {@code minPrecedence},
     * recursing with {@code precedence + 1} for the right-hand side so that operators of the same
     * precedence stay left-associative (each one gets picked up by this loop, not by the recursive
     * call).
     */
    private ParseResult<Expression> parseExpression(TokenStream tokens, int minPrecedence) {
        ParseResult<Expression> leftResult = parsePrimary(tokens);
        Expression left = leftResult.node();
        TokenStream rest = leftResult.rest();

        while (check(rest, TokenType.OPERATOR)) {
            OperatorDefinition operator = operators.get(peek(rest).lexeme());
            if (operator.precedence() < minPrecedence) {
                break;
            }
            ParseResult<Token> operatorToken = advance(rest);
            ParseResult<Expression> rightResult =
                    parseExpression(operatorToken.rest(), operator.precedence() + 1);
            left =
                    new BinaryExpression(
                            left,
                            operator,
                            rightResult.node(),
                            left.start(),
                            rightResult.node().end());
            rest = rightResult.rest();
        }
        return new ParseResult<>(left, rest);
    }

    private ParseResult<Expression> parsePrimary(TokenStream tokens) {
        Token token = peek(tokens);
        switch (token.type()) {
            case NUMBER_LITERAL -> {
                ParseResult<Token> consumed = advance(tokens);
                Expression expression =
                        new NumberLiteralExpression(
                                Double.parseDouble(token.lexeme()), token.start(), token.end());
                return new ParseResult<>(expression, consumed.rest());
            }
            case STRING_LITERAL -> {
                ParseResult<Token> consumed = advance(tokens);
                Expression expression =
                        new StringLiteralExpression(token.lexeme(), token.start(), token.end());
                return new ParseResult<>(expression, consumed.rest());
            }
            case IDENTIFIER -> {
                ParseResult<Token> consumed = advance(tokens);
                Expression expression =
                        new IdentifierExpression(token.lexeme(), token.start(), token.end());
                return new ParseResult<>(expression, consumed.rest());
            }
            case LEFT_PAREN -> {
                ParseResult<Token> leftParen = advance(tokens);
                ParseResult<Expression> inner = parseExpression(leftParen.rest());
                ParseResult<Token> rightParen =
                        expect(
                                inner.rest(),
                                TokenType.RIGHT_PAREN,
                                "Expected ')' to close expression");
                return new ParseResult<>(inner.node(), rightParen.rest());
            }
            default ->
                    throw new SyntaxException(
                            "Expected an expression but found '" + token.lexeme() + "'",
                            token.start(),
                            token.end());
        }
    }

    // --- Pure helpers over an immutable TokenStream — no shared cursor, nothing mutated. ---

    private static Token peek(TokenStream tokens) {
        return tokens.head();
    }

    private static boolean check(TokenStream tokens, TokenType type) {
        return peek(tokens).type() == type;
    }

    private static ParseResult<Token> advance(TokenStream tokens) {
        return new ParseResult<>(tokens.head(), tokens.tail());
    }

    private static ParseResult<Token> expect(
            TokenStream tokens, TokenType type, String errorMessage) {
        Token token = peek(tokens);
        if (token.type() != type) {
            throw new SyntaxException(
                    errorMessage + ", but found '" + token.lexeme() + "'",
                    token.start(),
                    token.end());
        }
        return advance(tokens);
    }
}
