package edu.austral.ingsis.printscript.common;

public enum TokenType {
    // Keywords
    LET,
    PRINTLN,

    // Literals and identifiers
    IDENTIFIER,
    NUMBER_LITERAL,
    STRING_LITERAL,

    // Symbols
    COLON,
    EQUALS,
    SEMICOLON,
    PLUS,
    MINUS,
    STAR,
    SLASH,
    LEFT_PAREN,
    RIGHT_PAREN,

    EOF
}
