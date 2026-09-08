package edu.austral.ingsis.printscript.parser;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.TokenStream;
import edu.austral.ingsis.printscript.common.ast.Statement;

public final class PrintScriptParser implements Parser {

    private final Map<String, OperatorDefinition> operators;

    public PrintScriptParser() {
        this(Set.of());
    }

    /** {@code extensionOperators} come from plugin modules found through {@code ServiceLoader}. */
    public PrintScriptParser(Set<OperatorDefinition> extensionOperators) {
        this.operators = CoreOperators.indexBySymbol(extensionOperators);
    }

    @Override
    public Iterator<Statement> parse(TokenStream tokens) {
        return new StatementIterator(tokens, operators);
    }
}
