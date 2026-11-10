package edu.austral.ingsis.printscript.common;

import java.math.BigDecimal;

/**
 * Renders a PrintScript number as text, in the only notation the language can read back: plain
 * digits, with no exponent and no trailing {@code .0}.
 */
public final class NumberText {

    private NumberText() {}

    public static String of(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return String.valueOf(value);
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
