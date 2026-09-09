package edu.austral.ingsis.printscript.plugins.modulo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.OperatorPrecedenceResolver;

import org.junit.jupiter.api.Test;

class ModuloOperatorTest {

    @Test
    void computesRemainder() {
        ModuloOperator operator = new ModuloOperator();

        assertEquals("%", operator.symbol());
        assertEquals(1.0, operator.apply(7, 3));
        assertEquals(0.0, operator.apply(6, 3));
    }

    @Test
    void bindsAtTheSameLevelAsMultiplicationAndDivision() {
        ModuloOperator operator = new ModuloOperator();

        Map<OperatorDefinition, Integer> levels =
                OperatorPrecedenceResolver.resolveLevels(
                        Set.of(
                                CoreOperators.PLUS,
                                CoreOperators.MINUS,
                                CoreOperators.MULTIPLY,
                                operator));

        assertEquals(levels.get(CoreOperators.MULTIPLY), levels.get(operator));
    }

    @Test
    void isDiscoverableViaServiceLoader() {
        boolean found =
                ServiceLoader.load(OperatorDefinition.class).stream()
                        .anyMatch(provider -> provider.type() == ModuloOperator.class);

        assertTrue(found, "META-INF/services registration for ModuloOperator is missing or wrong");
    }
}
