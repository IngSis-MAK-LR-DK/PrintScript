package edu.austral.ingsis.printscript.common;

/** Raised by the lexer when it finds a character sequence that cannot form a valid token. */
public class LexicalException extends PrintScriptException {

    public LexicalException(String message, Position start, Position end) {
        super(message, start, end);
    }
}
