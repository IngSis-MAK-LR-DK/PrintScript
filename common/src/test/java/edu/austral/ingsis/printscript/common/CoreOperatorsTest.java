package edu.austral.ingsis.printscript.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class CoreOperatorsTest {

    @Test
    void computesEachOperation() {
        assertEquals(5.0, CoreOperators.PLUS.apply(2, 3));
        assertEquals(2.0, CoreOperators.MINUS.apply(5, 3));
        assertEquals(6.0, CoreOperators.MULTIPLY.apply(2, 3));
        assertEquals(2.0, CoreOperators.DIVIDE.apply(6, 3));
    }

    @Test
    void multiplicationAndDivisionBindTighterThanAdditionAndSubtraction() {
        Map<OperatorDefinition, Integer> levels =
                OperatorPrecedenceResolver.resolveLevels(CoreOperators.all());

        assertTrue(levels.get(CoreOperators.MULTIPLY) > levels.get(CoreOperators.PLUS));
        assertTrue(levels.get(CoreOperators.DIVIDE) > levels.get(CoreOperators.MINUS));
        assertEquals(levels.get(CoreOperators.MULTIPLY), levels.get(CoreOperators.DIVIDE));
        assertEquals(levels.get(CoreOperators.PLUS), levels.get(CoreOperators.MINUS));
    }

    @Test
    void allReturnsTheFourCoreOperators() {
        assertEquals(4, CoreOperators.all().size());
        assertTrue(CoreOperators.all().contains(CoreOperators.PLUS));
        assertTrue(CoreOperators.all().contains(CoreOperators.MINUS));
        assertTrue(CoreOperators.all().contains(CoreOperators.MULTIPLY));
        assertTrue(CoreOperators.all().contains(CoreOperators.DIVIDE));
    }

    @Test
    void indexBySymbolIncludesTheCoreOperatorsEvenWithNoPlugins() {
        Map<String, OperatorDefinition> table = CoreOperators.indexBySymbol(Set.of());

        assertSame(CoreOperators.PLUS, table.get("+"));
        assertSame(CoreOperators.MINUS, table.get("-"));
        assertSame(CoreOperators.MULTIPLY, table.get("*"));
        assertSame(CoreOperators.DIVIDE, table.get("/"));
    }

    @Test
    void indexBySymbolAddsAPluginOperatorAlongsideTheCoreOnes() {
        OperatorDefinition modulo = stubOperator("%");

        Map<String, OperatorDefinition> table = CoreOperators.indexBySymbol(Set.of(modulo));

        assertSame(modulo, table.get("%"));
        assertSame(CoreOperators.PLUS, table.get("+"));
    }

    @Test
    void indexBySymbolThrowsWhenAPluginReusesACoreSymbol() {
        OperatorDefinition fakePlus = stubOperator("+");

        assertThrows(
                IllegalArgumentException.class,
                () -> CoreOperators.indexBySymbol(Set.of(fakePlus)));
    }

    @Test
    void indexBySymbolThrowsWhenTwoPluginsShareASymbol() {
        OperatorDefinition first = stubOperator("%");
        OperatorDefinition second = stubOperator("%");

        assertThrows(
                IllegalArgumentException.class,
                () -> CoreOperators.indexBySymbol(Set.of(first, second)));
    }

    private static OperatorDefinition stubOperator(String symbol) {
        return new OperatorDefinition() {
            @Override
            public String symbol() {
                return symbol;
            }

            @Override
            public OperatorPrecedence precedence() {
                return OperatorPrecedence.higherThan(CoreOperators.PLUS, CoreOperators.MINUS);
            }

            @Override
            public double apply(double left, double right) {
                return 0;
            }
        };
    }
}
