package edu.austral.ingsis.printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.austral.ingsis.printscript.common.ConfigFormat;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.lexer.PrintScriptLexer;
import edu.austral.ingsis.printscript.parser.PrintScriptParser;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PrintScriptFormatterTest {

    private final PrintScriptLexer lexer = new PrintScriptLexer();
    private final PrintScriptParser parser = new PrintScriptParser();
    private final PrintScriptFormatter formatter = new PrintScriptFormatter();
    private final FormatterConfigLoader configLoader = new FormatterConfigLoader();

    private String format(String source, FormatterConfig config) {
        Iterator<Statement> statements = parser.parse(lexer.tokenize(new StringReader(source)));
        return formatter.format(statements, config);
    }

    @Test
    void addsSpaceAroundColonAndEqualsWhenConfigured() {
        FormatterConfig config = new FormatterConfig(true, true, true, 0);

        String result = format("let x:number=12;", config);

        assertEquals("let x : number = 12;\n", result);
    }

    @Test
    void omitsSpaceBeforeColonWhenConfigured() {
        FormatterConfig config = new FormatterConfig(false, true, true, 0);

        String result = format("let x : number = 12;", config);

        assertEquals("let x: number = 12;\n", result);
    }

    @Test
    void insertsBlankLinesBeforePrintlnAsConfigured() {
        FormatterConfig config = new FormatterConfig(false, true, true, 2);

        String result = format("let x: number = 1;\nprintln(x);", config);

        assertEquals("let x: number = 1;\n\n\nprintln(x);\n", result);
    }

    @Test
    void alwaysPutsOneSpaceAroundOperatorsRegardlessOfConfig() {
        FormatterConfig config = new FormatterConfig(false, false, false, 0);

        String result = format("x=1+2;", config);

        assertEquals("x=1 + 2;\n", result);
    }

    @Test
    void loadsConfigFromYaml() {
        String yaml =
                """
                spaceBeforeColon: true
                spaceAfterColon: false
                spaceAroundEquals: false
                newLinesBeforePrintln: 1
                """;

        FormatterConfig config = configLoader.load(new StringReader(yaml), ConfigFormat.YAML);

        assertEquals(new FormatterConfig(true, false, false, 1), config);
    }

    @Test
    void formatsAnOperatorContributedByAPlugin() {
        OperatorDefinition modulo = stubModuloOperator();
        PrintScriptLexer pluggableLexer = new PrintScriptLexer(Set.of(modulo));
        PrintScriptParser pluggableParser = new PrintScriptParser(Set.of(modulo));
        FormatterConfig config = new FormatterConfig(false, true, true, 0);

        Iterator<Statement> statements =
                pluggableParser.parse(pluggableLexer.tokenize(new StringReader("x=a%b;")));
        String result = formatter.format(statements, config);

        assertEquals("x = a % b;\n", result);
    }

    private static OperatorDefinition stubModuloOperator() {
        return new OperatorDefinition() {
            @Override
            public String symbol() {
                return "%";
            }

            @Override
            public double apply(double left, double right) {
                return left % right;
            }
        };
    }
}
