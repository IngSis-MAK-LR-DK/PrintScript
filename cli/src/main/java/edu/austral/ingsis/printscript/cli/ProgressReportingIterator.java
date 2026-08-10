package edu.austral.ingsis.printscript.cli;

import edu.austral.ingsis.printscript.common.ast.Statement;
import java.util.Iterator;

final class ProgressReportingIterator implements Iterator<Statement> {

    private final Iterator<Statement> delegate;
    private final ProgressReporter reporter;

    ProgressReportingIterator(Iterator<Statement> delegate, ProgressReporter reporter) {
        this.delegate = delegate;
        this.reporter = reporter;
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = delegate.hasNext();
        if (!hasNext) {
            reporter.finish();
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
