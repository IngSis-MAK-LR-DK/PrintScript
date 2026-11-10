package edu.austral.ingsis.printscript.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NumberTextTest {

    @ParameterizedTest
    @CsvSource({
        "3, 3",
        "3.0, 3",
        "3.5, 3.5",
        "100, 100",
        "0, 0",
        "0.00001, 0.00001",
        "1e20, 100000000000000000000",
        "-2.5, -2.5",
        "Infinity, Infinity",
        "NaN, NaN"
    })
    void rendersNumbersInPlainNotation(double value, String expected) {
        assertEquals(expected, NumberText.of(value));
    }
}
