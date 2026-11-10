package edu.austral.ingsis.printscript.formatter;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.OperatorPrecedenceResolver;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;

public final class PrintScriptFormatter implements Formatter {

    private final Map<OperatorDefinition, Integer> precedenceLevels;

    public PrintScriptFormatter() {
        this(Set.of());
    }

    /** {@code extensionOperators} come from plugin modules found through {@code ServiceLoader}. */
    public PrintScriptFormatter(Set<OperatorDefinition> extensionOperators) {
        Map<String, OperatorDefinition> operators = CoreOperators.indexBySymbol(extensionOperators);
        this.precedenceLevels =
                OperatorPrecedenceResolver.resolveLevels(Set.copyOf(operators.values()));
    }

    @Override
    public void format(
            Iterator<Statement> statements, FormatterConfig config, Consumer<String> output) {
        StatementFormatter statementFormatter = new StatementFormatter(config, precedenceLevels);
        Statement previous = null;

        while (statements.hasNext()) {
            Statement statement = statements.next();
            if (previous instanceof PrintlnStatement && statement instanceof PrintlnStatement) {
                output.accept("\n".repeat(config.newLinesBeforePrintln()));
            }
            output.accept(statement.accept(statementFormatter) + "\n");
            previous = statement;
        }
    }
}
