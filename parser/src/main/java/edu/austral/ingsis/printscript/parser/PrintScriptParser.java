package edu.austral.ingsis.printscript.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.TokenStream;
import edu.austral.ingsis.printscript.common.ast.Statement;

public final class PrintScriptParser implements Parser {

    private final Map<String, OperatorDefinition> extensionOperators;

    public PrintScriptParser() {
        this(Set.of());
    }

    /**
     * {@code extensionOperators} are contributed by plugin modules discovered via ServiceLoader.
     */
    public PrintScriptParser(Set<OperatorDefinition> extensionOperators) {
        Map<String, OperatorDefinition> bySymbol = new HashMap<>();
        for (OperatorDefinition operator : extensionOperators) {
            OperatorDefinition previous = bySymbol.put(operator.symbol(), operator);
            if (previous != null) {
                throw new IllegalArgumentException(
                        "Two operator plugins both define the symbol '"
                                + operator.symbol()
                                + "': "
                                + previous.getClass().getName()
                                + " and "
                                + operator.getClass().getName());
            }
        }
        this.extensionOperators = Map.copyOf(bySymbol);
    }

    @Override
    public Iterator<Statement> parse(TokenStream tokens) {
        return new StatementIterator(tokens, extensionOperators);
    }
}
