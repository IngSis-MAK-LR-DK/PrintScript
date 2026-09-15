package edu.austral.ingsis.printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RawFormatterConfig#resolve()} alone, isolated from YAML/JSON parsing - what
 * actually fixes the Jackson-canonical-constructor gotcha is this method, not the loader that calls
 * it.
 */
class RawFormatterConfigTest {

    @Test
    void everyFieldAbsentResolvesToTheDomainDefault() {
        RawFormatterConfig raw = new RawFormatterConfig(null, null, null, null, null, null, null);

        assertEquals(FormatterConfig.defaultConfig(), raw.resolve());
    }

    @Test
    void anExplicitZeroIsRespectedAndNotOverwrittenByTheDefault() {
        // This is exactly what a primitive int can't tell apart from "absent" - the whole point
        // of boxing every field in RawFormatterConfig.
        RawFormatterConfig raw = new RawFormatterConfig(false, false, false, 0, 0, false, false);

        FormatterConfig resolved = raw.resolve();

        assertEquals(0, resolved.newLinesBeforePrintln());
        assertEquals(0, resolved.indentSize());
    }

    @Test
    void aMixOfPresentAndAbsentFieldsResolvesEachIndependently() {
        RawFormatterConfig raw = new RawFormatterConfig(true, null, null, 1, null, null, null);

        FormatterConfig resolved = raw.resolve();

        assertEquals(true, resolved.spaceBeforeColon());
        assertEquals(FormatterConfig.defaultConfig().spaceAfterColon(), resolved.spaceAfterColon());
        assertEquals(
                FormatterConfig.defaultConfig().spaceAroundEquals(), resolved.spaceAroundEquals());
        assertEquals(1, resolved.newLinesBeforePrintln());
        assertEquals(FormatterConfig.defaultConfig().indentSize(), resolved.indentSize());
        assertEquals(
                FormatterConfig.defaultConfig().spaceAroundParens(), resolved.spaceAroundParens());
    }
}
