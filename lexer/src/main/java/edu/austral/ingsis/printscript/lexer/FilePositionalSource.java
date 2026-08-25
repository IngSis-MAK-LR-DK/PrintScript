package edu.austral.ingsis.printscript.lexer;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.LongConsumer;

/**
 * {@link PositionalSource} backed by a real file, read via positional (non-cursor-mutating) I/O.
 */
public final class FilePositionalSource implements PositionalSource, Closeable {

    private static final int MAX_UTF8_CHAR_BYTES = 4;

    private final FileChannel channel;
    private final long sizeInBytes;
    private final LongConsumer onOffsetReached;

    public FilePositionalSource(Path file) throws IOException {
        this(file, offset -> {});
    }

    public FilePositionalSource(Path file, LongConsumer onOffsetReached) throws IOException {
        this.channel = FileChannel.open(file, StandardOpenOption.READ);
        this.sizeInBytes = channel.size();
        this.onOffsetReached = onOffsetReached;
    }

    @Override
    public CharRead readAt(long offset) {
        if (offset >= sizeInBytes) {
            return CharRead.endOfInput(offset);
        }
        try {
            ByteBuffer bytes = ByteBuffer.allocate(MAX_UTF8_CHAR_BYTES);
            while (bytes.hasRemaining() && channel.read(bytes, offset + bytes.position()) > 0) {
                // keep filling until we have up to 4 bytes or hit real EOF
            }
            bytes.flip();
            if (!bytes.hasRemaining()) {
                return CharRead.endOfInput(offset);
            }
            // True only if this 4-byte window actually reaches the real end of the file - NOT
            // just the end of our small scratch buffer. Telling the decoder "this is really all
            // there will ever be" (endOfInput=true) when we've merely truncated our own window
            // mid-character makes it report the trailing partial bytes as malformed instead of
            // "needs more input" - even though the character we actually wanted already decoded
            // successfully. Only the genuine end of the file may legitimately be malformed.
            boolean reachedRealEndOfFile = offset + bytes.limit() >= sizeInBytes;
            CharRead read = decodeFirstCodePoint(bytes, offset, reachedRealEndOfFile);
            onOffsetReached.accept(read.nextOffset());
            return read;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private CharRead decodeFirstCodePoint(ByteBuffer bytes, long offset, boolean endOfInput)
            throws IOException {
        // A fresh decoder per call, on purpose: CharsetDecoder is stateful/not thread-safe, and a
        // shared field would reintroduce exactly the hidden mutable state this class avoids.
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder(); // default action: REPORT
        CharBuffer chars = CharBuffer.allocate(1);
        CoderResult result = decoder.decode(bytes, chars, endOfInput);

        if (result.isOverflow() && chars.position() == 0) {
            // One code point that needs two chars (a surrogate pair) - retry with room for both.
            bytes.rewind();
            decoder.reset();
            chars = CharBuffer.allocate(2);
            result = decoder.decode(bytes, chars, endOfInput);
        }
        if (result.isMalformed() || result.isUnmappable()) {
            result.throwException();
        }
        int consumed = bytes.position();
        if (chars.position() == 0 || consumed <= 0) {
            throw new MalformedInputException(1); // guarantees forward progress; truncated at EOF
        }
        int codePoint =
                chars.position() == 2
                        ? Character.toCodePoint(chars.get(0), chars.get(1))
                        : chars.get(0);
        return new CharRead(codePoint, offset + consumed);
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
