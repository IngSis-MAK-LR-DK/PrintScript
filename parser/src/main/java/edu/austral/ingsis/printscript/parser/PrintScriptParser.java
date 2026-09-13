package edu.austral.ingsis.printscript.parser;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.OperatorPrecedenceResolver;
import edu.austral.ingsis.printscript.common.TokenStream;
import edu.austral.ingsis.printscript.common.Version;
import edu.austral.ingsis.printscript.common.ast.Statement;

public final class PrintScriptParser implements Parser {

    private final Map<String, OperatorDefinition> operators;
    private final Map<OperatorDefinition, Integer> precedenceLevels;
    private final Version version;

    public PrintScriptParser() {
        this(Set.of());
    }

    /** {@code extensionOperators} come from plugin modules found through {@code ServiceLoader}. */
    public PrintScriptParser(Set<OperatorDefinition> extensionOperators) {
        this(extensionOperators, Version.V1_0);
    }

    public PrintScriptParser(Set<OperatorDefinition> extensionOperators, Version version) {
        this.operators = CoreOperators.indexBySymbol(extensionOperators);
        this.precedenceLevels =
                OperatorPrecedenceResolver.resolveLevels(Set.copyOf(operators.values()));
        this.version = version;
    }

    @Override
    public Iterator<Statement> parse(TokenStream tokens) {
        Iterator<Statement> statements = new StatementIterator(tokens, operators, precedenceLevels);
        return new VersionValidator(version).validating(statements);
    }
}
