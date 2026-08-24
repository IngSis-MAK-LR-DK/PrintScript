package edu.austral.ingsis.printscript.cli;

import edu.austral.ingsis.printscript.common.TokenStream;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.lexer.FilePositionalSource;
import edu.austral.ingsis.printscript.lexer.Lexer;
import edu.austral.ingsis.printscript.parser.Parser;
import java.io.IOException;
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
        long totalBytes = Files.size(sourceFile);
        ProgressReporter reporter = new ProgressReporter(totalBytes);
        FilePositionalSource source = new FilePositionalSource(sourceFile, reporter::reachedOffset);

        TokenStream tokens = lexer.tokenize(source);
        Iterator<Statement> statements = parser.parse(tokens);

        return new ProgressReportingIterator(statements, reporter, source);
    }
}
