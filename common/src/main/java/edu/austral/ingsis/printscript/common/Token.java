package edu.austral.ingsis.printscript.common;

/**
 * @param lexeme the raw text of the token as it appears in the source, e.g. {@code "12"} or {@code
 *     "\"hello\""}
 */
public record Token(TokenType type, String lexeme, Position start, Position end) {}
