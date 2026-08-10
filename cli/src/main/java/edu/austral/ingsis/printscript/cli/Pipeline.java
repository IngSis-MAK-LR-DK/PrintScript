package edu.austral.ingsis.printscript.cli;

import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.lexer.Lexer;
import edu.austral.ingsis.printscript.parser.Parser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/** Wires source file -> lexer -> parser, reporting progress on the console as it goes. */
final class Pipeline {

    private final Lexer lexer;
    private final Parser parser;

    Pipeline(Lexer lexer, Parser parser) {
        this.lexer = lexer;
        this.parser = parser;
    }

    Iterator<Statement> parse(Path sourceFile) throws IOException {
        long totalChars = Files.size(sourceFile);
        Reader fileReader = Files.newBufferedReader(sourceFile, StandardCharsets.UTF_8);
        CountingReader countingReader = new CountingReader(fileReader);

        Iterator<Token> tokens = lexer.tokenize(countingReader);
        Iterator<Statement> statements = parser.parse(tokens);

        ProgressReporter reporter = new ProgressReporter(countingReader, totalChars);
        return new ProgressReportingIterator(statements, reporter);
    }
}
