package edu.austral.ingsis.printscript.parser;

import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.ast.Statement;
import java.util.Iterator;

public final class PrintScriptParser implements Parser {

    @Override
    public Iterator<Statement> parse(Iterator<Token> tokens) {
        return new StatementIterator(tokens);
    }
}
