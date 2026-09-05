package edu.austral.ingsis.printscript.lexer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.TokenStream;

public final class PrintScriptLexer implements Lexer {

    private final Map<String, OperatorDefinition> extensionOperators;

    public PrintScriptLexer() {
        this(Set.of());
    }

    /**
     * {@code extensionOperators} are contributed by plugin modules discovered via ServiceLoader.
     */
    public PrintScriptLexer(Set<OperatorDefinition> extensionOperators) {
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
    public TokenStream tokenize(PositionalSource source) {
        return new TokenScanner(source, extensionOperators).scan(Cursor.start());
    }
}
