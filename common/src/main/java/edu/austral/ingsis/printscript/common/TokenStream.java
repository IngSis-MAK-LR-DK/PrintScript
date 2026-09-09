package edu.austral.ingsis.printscript.common;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A lazy, immutable linked list of tokens. It doesn't cache anything: parsing only walks the stream
 * once, and reading a token has no side effects, so there's nothing to gain from remembering
 * results — re-scanning the same spot twice costs a bit of time, never a wrong answer.
 */
public final class TokenStream {

    private final Token head;
    private final Supplier<TokenStream> tail;

    public TokenStream(Token head, Supplier<TokenStream> tail) {
        this.head = Objects.requireNonNull(head);
        this.tail = Objects.requireNonNull(tail);
    }

    public Token head() {
        return head;
    }

    public boolean isAtEnd() {
        return head.type() == TokenType.EOF;
    }

    /** Doesn't mutate this stream — returns a new one, so any old reference stays valid. */
    public TokenStream tail() {
        return isAtEnd() ? this : tail.get();
    }
}
