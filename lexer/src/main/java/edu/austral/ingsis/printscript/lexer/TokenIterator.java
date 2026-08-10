package edu.austral.ingsis.printscript.lexer;

import edu.austral.ingsis.printscript.common.LexicalException;
import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.TokenType;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Scans one {@link Token} at a time from a {@link Reader}, keeping only a single character of
 * look-ahead in memory at any point. A token is buffered between {@link #hasNext()} and {@link
 * #next()} because computing "is there a next token" requires actually scanning it.
 */
final class TokenIterator implements Iterator<Token> {

    private static final Set<String> KEYWORDS = Set.of("let", "println");

    private final PushbackReader reader;
    private int line = 1;
    private int column = 1;
    private Token buffered;
    private boolean finished = false;

    TokenIterator(Reader source) {
        this.reader = new PushbackReader(source, 1);
    }

    @Override
    public boolean hasNext() {
        if (finished) {
            return false;
        }
        if (buffered == null) {
            buffered = scanToken();
        }
        return true;
    }

    @Override
    public Token next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more tokens");
        }
        Token token = buffered;
        buffered = null;
        if (token.type() == TokenType.EOF) {
            finished = true;
        }
        return token;
    }

    private Token scanToken() {
        skipWhitespace();

        Position start = currentPosition();
        int c = peek();
        if (c == -1) {
            return new Token(TokenType.EOF, "", start, start);
        }

        if (isDigit(c)) {
            return scanNumber(start);
        }
        if (isIdentifierStart(c)) {
            return scanIdentifierOrKeyword(start);
        }
        if (c == '"' || c == '\'') {
            return scanString(start, (char) c);
        }

        advance();
        TokenType symbol = symbolFor((char) c);
        if (symbol == null) {
            throw new LexicalException("Unexpected character '" + (char) c + "'", start, currentPosition());
        }
        return new Token(symbol, String.valueOf((char) c), start, currentPosition());
    }

    private TokenType symbolFor(char c) {
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

    private Token scanNumber(Position start) {
        StringBuilder lexeme = new StringBuilder();
        while (isDigit(peek())) {
            lexeme.append((char) advance());
        }
        if (peek() == '.') {
            lexeme.append((char) advance());
            while (isDigit(peek())) {
                lexeme.append((char) advance());
            }
        }
        return new Token(TokenType.NUMBER_LITERAL, lexeme.toString(), start, currentPosition());
    }

    private Token scanIdentifierOrKeyword(Position start) {
        StringBuilder lexeme = new StringBuilder();
        while (isIdentifierPart(peek())) {
            lexeme.append((char) advance());
        }
        String text = lexeme.toString();
        TokenType type =
                switch (text) {
                    case "let" -> TokenType.LET;
                    case "println" -> TokenType.PRINTLN;
                    default -> TokenType.IDENTIFIER;
                };
        return new Token(type, text, start, currentPosition());
    }

    private Token scanString(Position start, char quote) {
        advance(); // opening quote
        StringBuilder lexeme = new StringBuilder();
        int c = peek();
        while (c != -1 && c != quote && c != '\n') {
            lexeme.append((char) advance());
            c = peek();
        }
        if (c != quote) {
            throw new LexicalException("Unterminated string literal", start, currentPosition());
        }
        advance(); // closing quote
        return new Token(TokenType.STRING_LITERAL, lexeme.toString(), start, currentPosition());
    }

    private void skipWhitespace() {
        int c = peek();
        while (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
            advance();
            c = peek();
        }
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

    private Position currentPosition() {
        return new Position(line, column);
    }

    /** Reads one character ahead without consuming it or moving the current position. */
    private int peek() {
        try {
            int c = reader.read();
            if (c != -1) {
                reader.unread(c);
            }
            return c;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Consumes and returns the next character, advancing line/column bookkeeping. */
    private int advance() {
        try {
            int c = reader.read();
            if (c == '\n') {
                line++;
                column = 1;
            } else if (c != -1) {
                column++;
            }
            return c;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
