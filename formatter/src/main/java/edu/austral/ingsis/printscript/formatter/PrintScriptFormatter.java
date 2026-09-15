package edu.austral.ingsis.printscript.formatter;

import java.util.Iterator;

import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;

public final class PrintScriptFormatter implements Formatter {

    @Override
    public String format(Iterator<Statement> statements, FormatterConfig config) {
        StatementFormatter statementFormatter = new StatementFormatter(config);
        StringBuilder output = new StringBuilder();
        Statement previous = null;

        while (statements.hasNext()) {
            Statement statement = statements.next();
            // Only between two println calls back to back - not before the first println in the
            // file, and not when it follows some other kind of statement (see FormatterConfig's
            // newLinesBeforePrintln javadoc).
            if (previous instanceof PrintlnStatement && statement instanceof PrintlnStatement) {
                output.append("\n".repeat(config.newLinesBeforePrintln()));
            }
            output.append(statement.accept(statementFormatter)).append('\n');
            previous = statement;
        }

        return output.toString();
    }
}
