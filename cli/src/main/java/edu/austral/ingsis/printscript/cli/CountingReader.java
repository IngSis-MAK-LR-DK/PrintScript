package edu.austral.ingsis.printscript.cli;

import java.io.IOException;
import java.io.Reader;

/** Wraps a {@link Reader}, tracking how many characters have been consumed so far. */
final class CountingReader extends Reader {

    private final Reader delegate;
    private long charsRead = 0;

    CountingReader(Reader delegate) {
        this.delegate = delegate;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int read = delegate.read(cbuf, off, len);
        if (read > 0) {
            charsRead += read;
        }
        return read;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    long charsRead() {
        return charsRead;
    }
}
