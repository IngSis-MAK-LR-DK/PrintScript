package edu.austral.ingsis.printscript.parser;

import edu.austral.ingsis.printscript.common.SyntaxException;
import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.TokenType;
import java.util.Iterator;

/** One-token look-ahead over a lazily produced {@link Token} stream. */
final class PeekableTokenStream {

    private final Iterator<Token> tokens;
    private Token lookahead;

    PeekableTokenStream(Iterator<Token> tokens) {
        this.tokens = tokens;
    }

    Token peek() {
        if (lookahead == null) {
            lookahead = tokens.next();
        }
        return lookahead;
    }

    Token advance() {
        Token token = peek();
        lookahead = null;
        return token;
    }

    boolean check(TokenType type) {
        return peek().type() == type;
    }

    Token expect(TokenType type, String errorMessage) {
        Token token = peek();
        if (token.type() != type) {
            throw new SyntaxException(
                    errorMessage + ", but found '" + token.lexeme() + "'", token.start(), token.end());
        }
        return advance();
    }
}
