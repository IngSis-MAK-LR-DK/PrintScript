package edu.austral.ingsis.printscript.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilePositionalSourceTest {

    @TempDir Path tempDir;

    private Path writeFile(String content) throws IOException {
        Path file = tempDir.resolve("source.txt");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private List<Integer> codePointsOf(Path file) throws IOException {
        List<Integer> codePoints = new ArrayList<>();
        try (FilePositionalSource source = new FilePositionalSource(file)) {
            long offset = 0;
            while (true) {
                CharRead read = source.readAt(offset);
                if (read.isEndOfInput()) {
                    break;
                }
                codePoints.add(read.codePoint());
                offset = read.nextOffset();
            }
        }
        return codePoints;
    }

    @Test
    void readsPlainAsciiCharacters() throws IOException {
        Path file = writeFile("abc");

        assertEquals(List.of((int) 'a', (int) 'b', (int) 'c'), codePointsOf(file));
    }

    @Test
    void readsATwoByteUtf8Character() throws IOException {
        Path file = writeFile("café"); // é, 2 bytes in UTF-8

        assertEquals(List.of((int) 'c', (int) 'a', (int) 'f', (int) 'é'), codePointsOf(file));
    }

    @Test
    void readsAThreeByteUtf8Character() throws IOException {
        Path file = writeFile("€"); // €, 3 bytes in UTF-8

        assertEquals(List.of((int) '€'), codePointsOf(file));
    }

    @Test
    void readsAFourByteUtf8CharacterAsOneCodePoint() throws IOException {
        String emoji = "😀"; // 😀, U+1F600, 4 bytes in UTF-8, a surrogate pair in Java
        Path file = writeFile(emoji);

        List<Integer> codePoints = codePointsOf(file);

        assertEquals(1, codePoints.size());
        assertEquals(0x1F600, codePoints.get(0));
    }

    @Test
    void nextOffsetAdvancesByExactByteWidth() throws IOException {
        Path file = writeFile("aé"); // 'a' (1 byte) + é (2 bytes)

        try (FilePositionalSource source = new FilePositionalSource(file)) {
            CharRead first = source.readAt(0);
            assertEquals('a', first.codePoint());
            assertEquals(1, first.nextOffset());

            CharRead second = source.readAt(first.nextOffset());
            assertEquals('é', second.codePoint());
            assertEquals(3, second.nextOffset());

            assertTrue(source.readAt(second.nextOffset()).isEndOfInput());
        }
    }

    @Test
    void readingPastEndOfFileReturnsEndOfInput() throws IOException {
        Path file = writeFile("x");

        try (FilePositionalSource source = new FilePositionalSource(file)) {
            assertTrue(source.readAt(1).isEndOfInput());
            assertTrue(source.readAt(100).isEndOfInput());
        }
    }

    @Test
    void emptyFileIsImmediatelyAtEnd() throws IOException {
        Path file = writeFile("");

        try (FilePositionalSource source = new FilePositionalSource(file)) {
            assertTrue(source.readAt(0).isEndOfInput());
        }
    }

    @Test
    void reportsCumulativeOffsetThroughCallback() throws IOException {
        Path file = writeFile("abé"); // 1 + 1 + 2 = 4 bytes total

        List<Long> reportedOffsets = new ArrayList<>();
        try (FilePositionalSource source = new FilePositionalSource(file, reportedOffsets::add)) {
            long offset = 0;
            while (true) {
                CharRead read = source.readAt(offset);
                if (read.isEndOfInput()) {
                    break;
                }
                offset = read.nextOffset();
            }
        }

        assertEquals(List.of(1L, 2L, 4L), reportedOffsets);
    }

    @Test
    void tokenizesAStringLiteralWithMultiByteCharacters() throws IOException {
        Path file = writeFile("println(\"café😀\");");

        PrintScriptLexer lexer = new PrintScriptLexer();
        try (FilePositionalSource source = new FilePositionalSource(file)) {
            var tokens = lexer.tokenize(source);
            tokens = tokens.tail(); // println
            tokens = tokens.tail(); // (
            assertEquals(
                    edu.austral.ingsis.printscript.common.TokenType.STRING_LITERAL,
                    tokens.head().type());
            assertEquals("café😀", tokens.head().lexeme());
        }
    }
}
