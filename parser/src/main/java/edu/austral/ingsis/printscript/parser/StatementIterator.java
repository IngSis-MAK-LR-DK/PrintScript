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
 * {@code expression} isn't split into separate grammar levels for each precedence — an operator's
 * precedence comes from the operator table ({@code operators}, core operators plus whatever a
 * plugin contributes) and is applied dynamically in {@link #parseExpression(int)}, so a new
 * operator changes only that table, never this grammar.
 */
final class StatementIterator implements Iterator<Statement> {

    private final PeekableTokenStream stream;
    private final Map<String, OperatorDefinition> operators;

    StatementIterator(TokenStream tokens, Map<String, OperatorDefinition> operators) {
        this.stream = new PeekableTokenStream(tokens);
        this.operators = operators;
    }

    @Override
    public boolean hasNext() {
        return !stream.check(TokenType.EOF);
    }

    @Override
    public Statement next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more statements");
        }
        return parseStatement();
    }

    private Statement parseStatement() {
        Token current = stream.peek();
        return switch (current.type()) {
            case LET -> parseVariableDeclaration();
            case PRINTLN -> parsePrintln();
            case IDENTIFIER -> parseAssignment();
            default ->
                    throw new SyntaxException(
                            "Expected a statement but found '" + current.lexeme() + "'",
                            current.start(),
                            current.end());
        };
    }

    private Statement parseVariableDeclaration() {
        Token letToken = stream.expect(TokenType.LET, "Expected 'let'");
        Token name = stream.expect(TokenType.IDENTIFIER, "Expected a variable name");
        stream.expect(TokenType.COLON, "Expected ':' after variable name");
        Token type = stream.expect(TokenType.IDENTIFIER, "Expected a type name");

        Optional<Expression> initializer = Optional.empty();
        if (stream.check(TokenType.EQUALS)) {
            stream.advance();
            initializer = Optional.of(parseExpression());
        }

        Token semicolon = stream.expect(TokenType.SEMICOLON, "Expected ';' after declaration");
        return new VariableDeclarationStatement(
                name.lexeme(), type.lexeme(), initializer, letToken.start(), semicolon.end());
    }

    private Statement parseAssignment() {
        Token name = stream.expect(TokenType.IDENTIFIER, "Expected a variable name");
        stream.expect(TokenType.EQUALS, "Expected '=' after identifier");
        Expression value = parseExpression();
        Token semicolon = stream.expect(TokenType.SEMICOLON, "Expected ';' after assignment");
        return new AssignmentStatement(name.lexeme(), value, name.start(), semicolon.end());
    }

    private Statement parsePrintln() {
        Token printlnToken = stream.expect(TokenType.PRINTLN, "Expected 'println'");
        stream.expect(TokenType.LEFT_PAREN, "Expected '(' after 'println'");
        Expression argument = parseExpression();
        stream.expect(TokenType.RIGHT_PAREN, "Expected ')' after println argument");
        Token semicolon = stream.expect(TokenType.SEMICOLON, "Expected ';' after println call");
        return new PrintlnStatement(argument, printlnToken.start(), semicolon.end());
    }

    private Expression parseExpression() {
        return parseExpression(0);
    }

    /**
     * Precedence climbing: consumes operators whose precedence is at least {@code minPrecedence},
     * recursing with {@code precedence + 1} for the right-hand side so that operators of the same
     * precedence stay left-associative (each one gets picked up by this loop, not by the recursive
     * call).
     */
    private Expression parseExpression(int minPrecedence) {
        Expression left = parsePrimary();
        while (stream.check(TokenType.OPERATOR)) {
            OperatorDefinition operator = operators.get(stream.peek().lexeme());
            if (operator.precedence() < minPrecedence) {
                break;
            }
            stream.advance();
            Expression right = parseExpression(operator.precedence() + 1);
            left = new BinaryExpression(left, operator, right, left.start(), right.end());
        }
        return left;
    }

    private Expression parsePrimary() {
        Token token = stream.peek();
        switch (token.type()) {
            case NUMBER_LITERAL:
                stream.advance();
                return new NumberLiteralExpression(
                        Double.parseDouble(token.lexeme()), token.start(), token.end());
            case STRING_LITERAL:
                stream.advance();
                return new StringLiteralExpression(token.lexeme(), token.start(), token.end());
            case IDENTIFIER:
                stream.advance();
                return new IdentifierExpression(token.lexeme(), token.start(), token.end());
            case LEFT_PAREN:
                stream.advance();
                Expression inner = parseExpression();
                stream.expect(TokenType.RIGHT_PAREN, "Expected ')' to close expression");
                return inner;
            default:
                throw new SyntaxException(
                        "Expected an expression but found '" + token.lexeme() + "'",
                        token.start(),
                        token.end());
        }
    }
}
