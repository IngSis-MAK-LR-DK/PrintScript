package edu.austral.ingsis.printscript.lexer;

import java.util.Map;

import edu.austral.ingsis.printscript.common.LexicalException;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.TokenStream;
import edu.austral.ingsis.printscript.common.TokenType;

/**
 * Pure replacement for the old mutable {@code TokenIterator}: every method threads a {@link Cursor}
 * value instead of mutating fields, so scanning the same cursor twice always yields the same
 * result.
 */
final class TokenScanner {

    private final PositionalSource source;
    private final Map<String, OperatorDefinition> extensionOperators;

    TokenScanner(PositionalSource source, Map<String, OperatorDefinition> extensionOperators) {
        this.source = source;
        this.extensionOperators = extensionOperators;
    }

    TokenStream scan(Cursor cursor) {
        Cursor afterWhitespace = skipWhitespace(cursor);
        ScanResult result = scanToken(afterWhitespace);
        Cursor next = result.next();
        return new TokenStream(
                result.token(),
                () -> scan(next)); // for EOF, never invoked (see TokenStream.tail())
    }

    private ScanResult scanToken(Cursor cursor) {
        Position start = cursor.position();
        CharRead c = peek(cursor);
        if (c.isEndOfInput()) {
            Token eof = new Token(TokenType.EOF, "", start, start);
            return new ScanResult(eof, cursor);
        }
        if (isDigit(c.codePoint())) {
            return scanNumber(cursor, start);
        }
        if (isIdentifierStart(c.codePoint())) {
            return scanIdentifierOrKeyword(cursor, start);
        }
        if (c.codePoint() == '"' || c.codePoint() == '\'') {
            return scanString(cursor, start, c.codePoint());
        }

        Cursor after = cursor.advancedOver(c);
        TokenType symbol = symbolFor(c.codePoint());
        if (symbol != null) {
            return new ScanResult(
                    new Token(symbol, codePointToString(c.codePoint()), start, after.position()),
                    after);
        }
        String symbolText = codePointToString(c.codePoint());
        if (extensionOperators.containsKey(symbolText)) {
            return new ScanResult(
                    new Token(TokenType.EXTENSION_OPERATOR, symbolText, start, after.position()),
                    after);
        }
        throw new LexicalException(
                "Unexpected character '" + symbolText + "'", start, after.position());
    }

    private ScanResult scanNumber(Cursor cursor, Position start) {
        StringBuilder lexeme = new StringBuilder();
        Cursor current = cursor;
        while (isDigit(peek(current).codePoint())) {
            lexeme.appendCodePoint(peek(current).codePoint());
            current = current.advancedOver(peek(current));
        }
        if (peek(current).codePoint() == '.') {
            lexeme.append('.');
            current = current.advancedOver(peek(current));
            while (isDigit(peek(current).codePoint())) {
                lexeme.appendCodePoint(peek(current).codePoint());
                current = current.advancedOver(peek(current));
            }
        }
        return new ScanResult(
                new Token(TokenType.NUMBER_LITERAL, lexeme.toString(), start, current.position()),
                current);
    }

    private ScanResult scanIdentifierOrKeyword(Cursor cursor, Position start) {
        StringBuilder lexeme = new StringBuilder();
        Cursor current = cursor;
        while (isIdentifierPart(peek(current).codePoint())) {
            lexeme.appendCodePoint(peek(current).codePoint());
            current = current.advancedOver(peek(current));
        }
        String text = lexeme.toString();
        TokenType type =
                switch (text) {
                    case "let" -> TokenType.LET;
                    case "println" -> TokenType.PRINTLN;
                    default -> TokenType.IDENTIFIER;
                };
        return new ScanResult(new Token(type, text, start, current.position()), current);
    }

    private ScanResult scanString(Cursor cursor, Position start, int quote) {
        Cursor current = cursor.advancedOver(peek(cursor)); // opening quote
        StringBuilder lexeme = new StringBuilder();
        int c = peek(current).codePoint();
        while (c != CharRead.END_OF_INPUT && c != quote && c != '\n') {
            lexeme.appendCodePoint(c);
            current = current.advancedOver(peek(current));
            c = peek(current).codePoint();
        }
        if (c != quote) {
            throw new LexicalException("Unterminated string literal", start, current.position());
        }
        current = current.advancedOver(peek(current)); // closing quote
        return new ScanResult(
                new Token(TokenType.STRING_LITERAL, lexeme.toString(), start, current.position()),
                current);
    }

    private Cursor skipWhitespace(Cursor cursor) {
        Cursor current = cursor;
        while (isWhitespace(peek(current).codePoint())) {
            current = current.advancedOver(peek(current));
        }
        return current;
    }

    private CharRead peek(Cursor cursor) {
        return source.readAt(cursor.offset());
    }

    private static TokenType symbolFor(int c) {
        return switch (c) {
            case ':' -> TokenType.COLON;
            case '=' -> TokenType.EQUALS;
            case ';' -> TokenType.SEMICOLON;
            case '+' -> TokenType.PLUS;
            case '-' -> TokenType.MINUS;
            case '*' -> TokenType.STAR;
            case '/' -> TokenType.SLASH;
            case '(' -> TokenType.LEFT_PAREN;
            case ')' -> TokenType.RIGHT_PAREN;
            default -> null;
        };
    }

    private static boolean isWhitespace(int c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    private static boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isIdentifierStart(int c) {
        return Character.isLetter(c) || c == '_';
    }

    private static boolean isIdentifierPart(int c) {
        return isIdentifierStart(c) || isDigit(c);
    }

    private static String codePointToString(int codePoint) {
        return Character.toString(codePoint);
    }
}
