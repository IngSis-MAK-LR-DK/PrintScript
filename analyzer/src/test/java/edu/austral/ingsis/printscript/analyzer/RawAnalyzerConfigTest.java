package edu.austral.ingsis.printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RawAnalyzerConfig#resolve()} alone, isolated from YAML/JSON parsing - what
 * actually fixes the Jackson-canonical-constructor gotcha is this method, not the loader that calls
 * it.
 */
class RawAnalyzerConfigTest {

    @Test
    void everyFieldAbsentResolvesToTheDomainDefault() {
        RawAnalyzerConfig raw = new RawAnalyzerConfig(null, null, null, null);

        assertEquals(AnalyzerConfig.defaultConfig(), raw.resolve());
    }

    @Test
    void anExplicitFalseIsRespectedAndNotOverwrittenByTheDefault() {
        // This is exactly what a primitive boolean can't tell apart from "absent" - the whole
        // point of boxing every field in RawAnalyzerConfig.
        RawAnalyzerConfig raw =
                new RawAnalyzerConfig(false, IdentifierCase.CAMEL_CASE, false, false);

        AnalyzerConfig resolved = raw.resolve();

        assertEquals(false, resolved.identifierCaseCheckEnabled());
        assertEquals(false, resolved.printlnArgumentMustBeIdentifierOrLiteral());
        assertEquals(false, resolved.readArgumentMustBeIdentifierOrLiteral());
    }

    @Test
    void aMixOfPresentAndAbsentFieldsResolvesEachIndependently() {
        RawAnalyzerConfig raw = new RawAnalyzerConfig(false, IdentifierCase.SNAKE_CASE, null, null);

        AnalyzerConfig resolved = raw.resolve();

        assertEquals(false, resolved.identifierCaseCheckEnabled());
        assertEquals(IdentifierCase.SNAKE_CASE, resolved.identifierCase());
        assertEquals(
                AnalyzerConfig.defaultConfig().printlnArgumentMustBeIdentifierOrLiteral(),
                resolved.printlnArgumentMustBeIdentifierOrLiteral());
        assertEquals(
                AnalyzerConfig.defaultConfig().readArgumentMustBeIdentifierOrLiteral(),
                resolved.readArgumentMustBeIdentifierOrLiteral());
    }
}
