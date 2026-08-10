package edu.austral.ingsis.printscript.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CliArgumentsTest {

    @Test
    void parsesOperationAndFile() {
        CliArguments arguments = CliArguments.parse(new String[] {"execution", "program.prs"});

        assertEquals(Operation.EXECUTION, arguments.operation());
        assertEquals(Path.of("program.prs"), arguments.sourceFile());
        assertEquals("1.0", arguments.version());
        assertTrue(arguments.configFile().isEmpty());
    }

    @Test
    void parsesOperationCaseInsensitively() {
        CliArguments arguments = CliArguments.parse(new String[] {"Formatting", "program.prs"});

        assertEquals(Operation.FORMATTING, arguments.operation());
    }

    @Test
    void parsesConfigFlag() {
        CliArguments arguments =
                CliArguments.parse(new String[] {"formatting", "program.prs", "--config", "rules.yaml"});

        assertEquals(Path.of("rules.yaml"), arguments.configFile().orElseThrow());
    }

    @Test
    void throwsWhenTooFewArguments() {
        assertThrows(CliUsageException.class, () -> CliArguments.parse(new String[] {"execution"}));
    }

    @Test
    void throwsOnUnknownOperation() {
        assertThrows(CliUsageException.class, () -> CliArguments.parse(new String[] {"compile", "program.prs"}));
    }

    @Test
    void throwsOnUnsupportedVersion() {
        assertThrows(
                CliUsageException.class,
                () -> CliArguments.parse(new String[] {"execution", "program.prs", "--version", "2.0"}));
    }

    @Test
    void throwsOnUnknownFlag() {
        assertThrows(
                CliUsageException.class,
                () -> CliArguments.parse(new String[] {"execution", "program.prs", "--bogus", "x"}));
    }
}
