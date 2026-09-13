package edu.austral.ingsis.printscript.common;

public enum TokenType {
    // Keywords
    LET,
    PRINTLN,

    // Literals and identifiers
    IDENTIFIER,
    NUMBER_LITERAL,
    STRING_LITERAL,
    BOOLEAN_LITERAL,

    // Symbols
    COLON,
    EQUALS,
    SEMICOLON,
    LEFT_PAREN,
    RIGHT_PAREN,

    // Any binary operator — core (+, -, *, /) or plugin-contributed. Which one it is gets
    // resolved by looking up the token's lexeme in the operator table (see OperatorDefinition).
    OPERATOR,

    EOF
}
