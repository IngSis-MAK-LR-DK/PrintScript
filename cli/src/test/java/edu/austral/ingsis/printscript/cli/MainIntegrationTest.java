package edu.austral.ingsis.printscript.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end tests driven through {@link Main#main(String[])} (happy paths) and {@link
 * Main#run(String[])} (error paths - {@code run} never calls {@code System.exit}, so every catch
 * branch in {@link Main} can be exercised safely here without killing the test JVM).
 */
class MainIntegrationTest {

    @TempDir Path tempDir;

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
    private final ByteArrayOutputStream capturedErr = new ByteArrayOutputStream();

    @BeforeEach
    void redirectStdStreams() {
        System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(capturedErr, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreStdStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private Path writeSource(String content) throws IOException {
        Path file = tempDir.resolve("program.prs");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private String output() {
        return capturedOut.toString(StandardCharsets.UTF_8);
    }

    private String errorOutput() {
        return capturedErr.toString(StandardCharsets.UTF_8);
    }

    @Test
    void executionPrintsProgramOutput() throws IOException {
        Path source = writeSource("let name: string = \"World\";\nprintln(\"Hello \" + name);");

        Main.main(new String[] {"execution", source.toString()});

        assertTrue(output().contains("Hello World"));
    }

    @Test
    void validationSucceedsForAWellFormedProgram() throws IOException {
        Path source = writeSource("let x: number = 1;\nprintln(x);");

        Main.main(new String[] {"validation", source.toString()});

        assertTrue(output().contains("OK"));
    }

    @Test
    void formattingPrintsFormattedSource() throws IOException {
        Path source = writeSource("let x:number=1;");

        Main.main(new String[] {"formatting", source.toString()});

        assertTrue(output().contains("let x: number = 1;"));
    }

    @Test
    void analyzingReportsOkForCompliantCode() throws IOException {
        Path source = writeSource("let myVar: number = 1;\nprintln(myVar);");

        Main.main(new String[] {"analyzing", source.toString()});

        assertTrue(output().contains("OK"));
    }

    @Test
    void formattingUsesProvidedYamlConfig() throws IOException {
        Path source = writeSource("let x:number=1;");
        Path config = tempDir.resolve("rules.yaml");
        Files.writeString(
                config,
                """
                spaceBeforeColon: true
                spaceAfterColon: true
                spaceAroundEquals: false
                newLinesBeforePrintln: 0
                """);

        Main.main(new String[] {"formatting", source.toString(), "--config", config.toString()});

        assertTrue(output().contains("let x : number=1;"));
    }

    /**
     * Proves the operator-plugin mechanism end-to-end through the real Gradle dependency graph:
     * {@code cli} only has a {@code runtimeOnly} dependency on {@code plugins:modulo-operator} (see
     * cli/build.gradle.kts), and {@link Main} discovers it purely via {@link
     * java.util.ServiceLoader} at runtime. Nothing in this test wires the plugin in manually.
     */
    @Test
    void executesAnOperatorContributedByAnInstalledPlugin() throws IOException {
        Path source = writeSource("println(7 % 3);");

        Main.main(new String[] {"execution", source.toString()});

        assertTrue(output().contains("1"));
    }

    @Test
    void executionHandlesMultiByteUtf8Source() throws IOException {
        Path source = writeSource("let greeting: string = \"café 🙂\";\nprintln(greeting);");

        Main.main(new String[] {"execution", source.toString()});

        assertTrue(output().contains("café 🙂"));
    }

    @Test
    void analyzingReportsFindingsAndExitCodeWhenViolationsExist() throws IOException {
        Path source = writeSource("let my_var: number = 1;\nprintln(my_var);");

        int exitCode = Main.run(new String[] {"analyzing", source.toString()});

        assertEquals(1, exitCode);
        assertTrue(output().contains("my_var"));
    }

    @Test
    void analyzingUsesProvidedYamlConfig() throws IOException {
        Path source = writeSource("let my_var: number = 1;\nprintln(my_var);");
        Path config = tempDir.resolve("rules.yaml");
        Files.writeString(
                config,
                """
                identifierCaseCheckEnabled: false
                identifierCase: CAMEL_CASE
                printlnArgumentMustBeIdentifierOrLiteral: true
                """);

        int exitCode =
                Main.run(
                        new String[] {
                            "analyzing", source.toString(), "--config", config.toString()
                        });

        assertEquals(0, exitCode);
        assertTrue(output().contains("OK"));
    }

    @Test
    void runReturnsUsageExitCodeOnBadArguments() {
        int exitCode = Main.run(new String[] {"execution"});

        assertEquals(2, exitCode);
        assertTrue(errorOutput().contains("Usage:"));
    }

    @Test
    void runReturnsErrorExitCodeOnSyntaxError() throws IOException {
        Path source = writeSource("let x number = 1;");

        int exitCode = Main.run(new String[] {"execution", source.toString()});

        assertEquals(1, exitCode);
        assertTrue(errorOutput().contains("SyntaxException"));
    }

    @Test
    void runReturnsErrorExitCodeWhenSourceFileDoesNotExist() {
        Path missing = tempDir.resolve("does-not-exist.prs");

        int exitCode = Main.run(new String[] {"execution", missing.toString()});

        assertEquals(1, exitCode);
        assertTrue(errorOutput().contains("Could not read the source file"));
    }

    @Test
    void runReturnsErrorExitCodeOnMalformedUtf8Source() throws IOException {
        Path source = tempDir.resolve("malformed.prs");
        // 0xC3 is a valid lead byte for a 2-byte UTF-8 sequence, but the file ends right there.
        Files.write(source, new byte[] {'x', (byte) 0xC3});

        int exitCode = Main.run(new String[] {"execution", source.toString()});

        assertEquals(1, exitCode);
        assertTrue(errorOutput().contains("Could not read the source file"));
    }
}
