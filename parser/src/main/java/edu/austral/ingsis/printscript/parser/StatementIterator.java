package edu.austral.ingsis.printscript.parser;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.SyntaxException;
import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.TokenType;
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
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Recursive-descent parser for the PrintScript 1.0 grammar:
 *
 * <pre>
 * statement   := declaration | assignment | println
 * declaration := "let" IDENTIFIER ":" IDENTIFIER ("=" expression)? ";"
 * assignment  := IDENTIFIER "=" expression ";"
 * println     := "println" "(" expression ")" ";"
 * expression  := term (("+" | "-") term)*
 * term        := primary (("*" | "/") primary)*
 * primary     := NUMBER | STRING | IDENTIFIER | "(" expression ")"
 * </pre>
 */
final class StatementIterator implements Iterator<Statement> {

    private final PeekableTokenStream stream;
    private final Map<String, OperatorDefinition> extensionOperators;

    StatementIterator(Iterator<Token> tokens, Map<String, OperatorDefinition> extensionOperators) {
        this.stream = new PeekableTokenStream(tokens);
        this.extensionOperators = extensionOperators;
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
        return parseAdditive();
    }

    private Expression parseAdditive() {
        Expression left = parseMultiplicative();
        while (stream.check(TokenType.PLUS) || stream.check(TokenType.MINUS)) {
            Token operatorToken = stream.advance();
            Expression right = parseMultiplicative();
            BinaryOperator operator =
                    operatorToken.type() == TokenType.PLUS ? BinaryOperator.PLUS : BinaryOperator.MINUS;
            left = new BinaryExpression(left, operator, right, left.start(), right.end());
        }
        return left;
    }

    private Expression parseMultiplicative() {
        Expression left = parsePrimary();
        while (stream.check(TokenType.STAR)
                || stream.check(TokenType.SLASH)
                || stream.check(TokenType.EXTENSION_OPERATOR)) {
            Token operatorToken = stream.advance();
            Expression right = parsePrimary();
            if (operatorToken.type() == TokenType.EXTENSION_OPERATOR) {
                OperatorDefinition operator = extensionOperators.get(operatorToken.lexeme());
                left = new ExtendedBinaryExpression(left, operator, right, left.start(), right.end());
            } else {
                BinaryOperator operator =
                        operatorToken.type() == TokenType.STAR ? BinaryOperator.MULTIPLY : BinaryOperator.DIVIDE;
                left = new BinaryExpression(left, operator, right, left.start(), right.end());
            }
        }
        return left;
    }

    private Expression parsePrimary() {
        Token token = stream.peek();
        switch (token.type()) {
            case NUMBER_LITERAL:
                stream.advance();
                return new NumberLiteralExpression(Double.parseDouble(token.lexeme()), token.start(), token.end());
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
                        "Expected an expression but found '" + token.lexeme() + "'", token.start(), token.end());
        }
    }
}
