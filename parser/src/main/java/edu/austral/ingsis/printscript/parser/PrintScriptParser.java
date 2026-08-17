package edu.austral.ingsis.printscript.parser;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.ast.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class PrintScriptParser implements Parser {

    private final Map<String, OperatorDefinition> extensionOperators;

    public PrintScriptParser() {
        this(Set.of());
    }

    /** {@code extensionOperators} are contributed by plugin modules discovered via ServiceLoader. */
    public PrintScriptParser(Set<OperatorDefinition> extensionOperators) {
        Map<String, OperatorDefinition> bySymbol = new HashMap<>();
        for (OperatorDefinition operator : extensionOperators) {
            bySymbol.put(operator.symbol(), operator);
        }
        this.extensionOperators = Map.copyOf(bySymbol);
    }

    @Override
    public Iterator<Statement> parse(Iterator<Token> tokens) {
        return new StatementIterator(tokens, extensionOperators);
    }
}
