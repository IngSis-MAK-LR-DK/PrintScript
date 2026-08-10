package edu.austral.ingsis.printscript.lexer;

import edu.austral.ingsis.printscript.common.Token;
import java.io.Reader;
import java.util.Iterator;

public final class PrintScriptLexer implements Lexer {

    @Override
    public Iterator<Token> tokenize(Reader source) {
        return new TokenIterator(source);
    }
}
