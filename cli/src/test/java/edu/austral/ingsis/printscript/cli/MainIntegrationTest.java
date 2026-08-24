package edu.austral.ingsis.printscript.cli;

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
 * End-to-end tests driven through {@link Main#main(String[])}. Only exercises paths that do not
 * call {@code System.exit}, since that would kill the test JVM.
 */
class MainIntegrationTest {

    @TempDir Path tempDir;

    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();

    @BeforeEach
    void redirectStdOut() {
        System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreStdOut() {
        System.setOut(originalOut);
    }

    private Path writeSource(String content) throws IOException {
        Path file = tempDir.resolve("program.prs");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private String output() {
        return capturedOut.toString(StandardCharsets.UTF_8);
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
}
