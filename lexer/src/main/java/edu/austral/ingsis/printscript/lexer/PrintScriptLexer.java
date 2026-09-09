package edu.austral.ingsis.printscript.lexer;

import java.util.Map;
import java.util.Set;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.TokenStream;

public final class PrintScriptLexer implements Lexer {

    private final Map<String, OperatorDefinition> operators;

    public PrintScriptLexer() {
        this(Set.of());
    }

    /** {@code extensionOperators} come from plugin modules found through {@code ServiceLoader}. */
    public PrintScriptLexer(Set<OperatorDefinition> extensionOperators) {
        this.operators = CoreOperators.indexBySymbol(extensionOperators);
    }

    @Override
    public TokenStream tokenize(PositionalSource source) {
        return new TokenScanner(source, operators, CoreKeywords.ALL).scan(Cursor.start());
    }
}
