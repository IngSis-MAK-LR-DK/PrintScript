package edu.austral.ingsis.printscript.formatter;

import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;
import java.util.Iterator;

public final class PrintScriptFormatter implements Formatter {

    @Override
    public String format(Iterator<Statement> statements, FormatterConfig config) {
        StatementFormatter statementFormatter = new StatementFormatter(config);
        StringBuilder output = new StringBuilder();
        boolean first = true;

        while (statements.hasNext()) {
            Statement statement = statements.next();
            if (!first && statement instanceof PrintlnStatement) {
                output.append("\n".repeat(config.newLinesBeforePrintln()));
            }
            output.append(statement.accept(statementFormatter)).append('\n');
            first = false;
        }

        return output.toString();
    }
}
