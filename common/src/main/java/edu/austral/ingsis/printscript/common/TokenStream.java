package edu.austral.ingsis.printscript.common;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * An immutable, lazy cons-list of tokens. Deliberately unmemoized: every read this compiler does
 * is pure (see {@code PositionalSource}), and parsing is single-pass (each node's {@link #tail()}
 * is forced at most once in practice), so caching would add complexity for no real benefit.
 * Re-forcing a node twice is safe — just redundant work, never a different or wrong answer.
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

    /** Non-destructive: returns a new stream reference; {@code this} remains valid and unchanged. */
    public TokenStream tail() {
        return isAtEnd() ? this : tail.get();
    }
}
