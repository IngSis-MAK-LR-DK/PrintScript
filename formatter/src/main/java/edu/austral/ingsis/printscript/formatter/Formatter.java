package edu.austral.ingsis.printscript.formatter;

import java.util.Iterator;

import edu.austral.ingsis.printscript.common.ast.Statement;

/** Pretty-prints a parsed program back into source code, following a {@link FormatterConfig}. */
public interface Formatter {

    String format(Iterator<Statement> statements, FormatterConfig config);
}
