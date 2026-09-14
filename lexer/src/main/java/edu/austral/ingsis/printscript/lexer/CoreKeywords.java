package edu.austral.ingsis.printscript.lexer;

import java.util.Map;

import edu.austral.ingsis.printscript.common.TokenType;

/**
 * The reserved words of the language, as data instead of a hardcoded {@code switch}. Adding a
 * keyword in a future version means adding an entry here (plus a {@code TokenType} constant and a
 * parser for whatever statement it introduces) rather than editing a switch that nothing forces to
 * stay in sync with the rest of the lexer.
 */
final class CoreKeywords {

    static final Map<String, TokenType> ALL =
            Map.of(
                    "let", TokenType.LET,
                    "const", TokenType.CONST,
                    "println", TokenType.PRINTLN,
                    "if", TokenType.IF,
                    "else", TokenType.ELSE,
                    "readInput", TokenType.READ_INPUT,
                    "readEnv", TokenType.READ_ENV,
                    "true", TokenType.BOOLEAN_LITERAL,
                    "false", TokenType.BOOLEAN_LITERAL);

    private CoreKeywords() {}
}
