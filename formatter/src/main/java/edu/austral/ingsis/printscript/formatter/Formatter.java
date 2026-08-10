package edu.austral.ingsis.printscript.formatter;

import edu.austral.ingsis.printscript.common.ast.Statement;
import java.util.Iterator;

/** Pretty-prints a PrintScript AST back into source code following a {@link FormatterConfig}. */
public interface Formatter {

    String format(Iterator<Statement> statements, FormatterConfig config);
}
