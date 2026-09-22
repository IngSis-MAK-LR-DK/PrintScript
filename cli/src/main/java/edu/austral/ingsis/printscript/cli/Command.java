package edu.austral.ingsis.printscript.cli;

import java.io.IOException;

/**
 * One CLI operation: validate, execute, format or analyze.
 *
 * @return the exit code is — 0 on success, nonzero if the tool ran fine but has something to
 *     report, like the analyzer finding rule violations.
 */
interface Command {

    int run(CliArguments arguments) throws IOException;
}
