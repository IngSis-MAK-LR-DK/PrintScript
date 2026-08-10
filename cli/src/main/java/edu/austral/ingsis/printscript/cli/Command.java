package edu.austral.ingsis.printscript.cli;

import java.io.IOException;

/**
 * One CLI operation (validate, execute, format, analyze).
 *
 * @return the process exit code: {@code 0} on success, non-zero if the tool ran fine but found
 *     something to report (e.g. the analyzer finding rule violations).
 */
interface Command {

    int run(CliArguments arguments) throws IOException;
}
