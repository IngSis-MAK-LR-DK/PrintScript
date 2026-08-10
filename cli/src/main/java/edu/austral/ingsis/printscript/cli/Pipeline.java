package edu.austral.ingsis.printscript.cli;

import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.lexer.PrintScriptLexer;
import edu.austral.ingsis.printscript.parser.PrintScriptParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/** Wires source file -> lexer -> parser, reporting progress on the console as it goes. */
final class Pipeline {

    private static final PrintScriptLexer LEXER = new PrintScriptLexer();
    private static final PrintScriptParser PARSER = new PrintScriptParser();

    private Pipeline() {}

    static Iterator<Statement> parse(Path sourceFile) throws IOException {
        long totalChars = Files.size(sourceFile);
        Reader fileReader = Files.newBufferedReader(sourceFile, StandardCharsets.UTF_8);
        CountingReader countingReader = new CountingReader(fileReader);

        Iterator<Token> tokens = LEXER.tokenize(countingReader);
        Iterator<Statement> statements = PARSER.parse(tokens);

        ProgressReporter reporter = new ProgressReporter(countingReader, totalChars);
        return new ProgressReportingIterator(statements, reporter);
    }
}
