package edu.austral.ingsis.printscript.formatter;

import java.util.Iterator;
import java.util.function.Consumer;

import edu.austral.ingsis.printscript.common.ast.Statement;

/** Pretty-prints a parsed program back into source code, following a {@link FormatterConfig}. */
public interface Formatter {

    /**
     * Hands the formatted program to {@code output} piece by piece, as each statement is read, so a
     * source too large for memory can be formatted as long as {@code output} doesn't keep it all.
     */
    void format(Iterator<Statement> statements, FormatterConfig config, Consumer<String> output);

    /** Convenience for programs small enough to hold in memory as a single string. */
    default String format(Iterator<Statement> statements, FormatterConfig config) {
        StringBuilder text = new StringBuilder();
        format(statements, config, text::append);
        return text.toString();
    }
}
