package edu.austral.ingsis.printscript.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TokenStreamTest {

    private static final Position P = new Position(1, 1);

    private static Token token(TokenType type) {
        return new Token(type, "", P, P);
    }

    /** A tail supplier for a node that this test never calls {@code .tail()} on. */
    private static TokenStream unreachableTail() {
        return new TokenStream(
                token(TokenType.EOF),
                () -> {
                    throw new AssertionError("should never be invoked");
                });
    }

    @Test
    void headReturnsTheTokenItWasBuiltWith() {
        Token head = token(TokenType.LET);

        TokenStream stream = new TokenStream(head, TokenStreamTest::unreachableTail);

        assertSame(head, stream.head());
    }

    @Test
    void isAtEndIsFalseForANonEofHead() {
        TokenStream stream =
                new TokenStream(token(TokenType.LET), TokenStreamTest::unreachableTail);

        assertFalse(stream.isAtEnd());
    }

    @Test
    void isAtEndIsTrueForAnEofHead() {
        TokenStream stream =
                new TokenStream(
                        token(TokenType.EOF),
                        () -> {
                            throw new AssertionError("should never be invoked once at EOF");
                        });

        assertTrue(stream.isAtEnd());
    }

    @Test
    void tailForcesTheLazySupplierExactlyOnce() {
        Token second = token(TokenType.EOF);
        TokenStream stream =
                new TokenStream(
                        token(TokenType.LET),
                        () -> new TokenStream(second, TokenStreamTest::unreachableTail));

        TokenStream tail = stream.tail();

        assertSame(second, tail.head());
    }

    @Test
    void tailIsASelfLoopOnceAtEnd() {
        TokenStream eof =
                new TokenStream(
                        token(TokenType.EOF),
                        () -> {
                            throw new AssertionError(
                                    "the supplier must never be invoked once isAtEnd() is true");
                        });

        assertSame(eof, eof.tail());
    }

    @Test
    void reForcingTheSameNodeGivesAnEqualButIndependentResult() {
        Token second = token(TokenType.SEMICOLON);
        TokenStream stream =
                new TokenStream(
                        token(TokenType.LET),
                        () -> new TokenStream(second, TokenStreamTest::unreachableTail));

        TokenStream first = stream.tail();
        TokenStream again = stream.tail();

        assertEquals(first.head(), again.head());
    }
}
