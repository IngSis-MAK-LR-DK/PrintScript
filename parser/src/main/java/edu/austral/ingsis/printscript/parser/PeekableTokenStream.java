package edu.austral.ingsis.printscript.parser;

import edu.austral.ingsis.printscript.common.SyntaxException;
import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.TokenStream;
import edu.austral.ingsis.printscript.common.TokenType;

/**
 * A cursor into an immutable {@link TokenStream}. The stream field is still mutable, it only ever
 * gets reassigned to point at a new, already-valid immutable value. The node it pointed to before
 * stays untouched and usable; nothing is destructively consumed.
 */
final class PeekableTokenStream {

    private TokenStream stream;

    PeekableTokenStream(TokenStream stream) {
        this.stream = stream;
    }

    Token peek() {
        return stream.head();
    }

    Token advance() {
        Token token = stream.head();
        stream = stream.tail();
        return token;
    }

    boolean check(TokenType type) {
        return peek().type() == type;
    }

    Token expect(TokenType type, String errorMessage) {
        Token token = peek();
        if (token.type() != type) {
            throw new SyntaxException(
                    errorMessage + ", but found '" + token.lexeme() + "'",
                    token.start(),
                    token.end());
        }
        return advance();
    }
}
