package edu.austral.ingsis.printscript.lexer;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.TokenStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class PrintScriptLexer implements Lexer {

    private final Map<String, OperatorDefinition> extensionOperators;

    public PrintScriptLexer() {
        this(Set.of());
    }

    /** {@code extensionOperators} are contributed by plugin modules discovered via ServiceLoader. */
    public PrintScriptLexer(Set<OperatorDefinition> extensionOperators) {
        Map<String, OperatorDefinition> bySymbol = new HashMap<>();
        for (OperatorDefinition operator : extensionOperators) {
            bySymbol.put(operator.symbol(), operator);
        }
        this.extensionOperators = Map.copyOf(bySymbol);
    }

    @Override
    public TokenStream tokenize(PositionalSource source) {
        return new TokenScanner(source, extensionOperators).scan(Cursor.start());
    }
}
