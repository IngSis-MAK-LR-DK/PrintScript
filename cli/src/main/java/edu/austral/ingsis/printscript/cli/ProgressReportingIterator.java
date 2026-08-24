package edu.austral.ingsis.printscript.cli;

import edu.austral.ingsis.printscript.common.ast.Statement;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;

final class ProgressReportingIterator implements Iterator<Statement> {

    private final Iterator<Statement> delegate;
    private final ProgressReporter reporter;
    private final Closeable source;

    ProgressReportingIterator(Iterator<Statement> delegate, ProgressReporter reporter, Closeable source) {
        this.delegate = delegate;
        this.reporter = reporter;
        this.source = source;
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = delegate.hasNext();
        if (!hasNext) {
            reporter.finish();
            try {
                source.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return hasNext;
    }

    @Override
    public Statement next() {
        Statement statement = delegate.next();
        reporter.report();
        return statement;
    }
}
