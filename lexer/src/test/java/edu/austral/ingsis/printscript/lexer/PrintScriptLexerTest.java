package edu.austral.ingsis.printscript.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.austral.ingsis.printscript.common.LexicalException;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.TokenStream;
import edu.austral.ingsis.printscript.common.TokenType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PrintScriptLexerTest {

    private final PrintScriptLexer lexer = new PrintScriptLexer();

    private List<Token> tokenize(String source) {
        return drain(lexer.tokenize(new StringPositionalSource(source)));
    }

    private static List<Token> drain(TokenStream stream) {
        List<Token> tokens = new ArrayList<>();
        TokenStream current = stream;
        while (true) {
            tokens.add(current.head());
            if (current.isAtEnd()) {
                break;
            }
            current = current.tail();
        }
        return tokens;
    }

    @Test
    void tokenizesVariableDeclaration() {
        List<Token> tokens = tokenize("let x: number = 12;");

        List<TokenType> types = tokens.stream().map(Token::type).toList();
        assertEquals(
                List.of(
                        TokenType.LET,
                        TokenType.IDENTIFIER,
                        TokenType.COLON,
                        TokenType.IDENTIFIER,
                        TokenType.EQUALS,
                        TokenType.NUMBER_LITERAL,
                        TokenType.SEMICOLON,
                        TokenType.EOF),
                types);
        assertEquals("x", tokens.get(1).lexeme());
        assertEquals("number", tokens.get(3).lexeme());
        assertEquals("12", tokens.get(5).lexeme());
    }

    @Test
    void tokenizesStringLiteralsWithBothQuoteStyles() {
        List<Token> tokens = tokenize("\"hello\" 'world'");

        assertEquals(TokenType.STRING_LITERAL, tokens.get(0).type());
        assertEquals("hello", tokens.get(0).lexeme());
        assertEquals(TokenType.STRING_LITERAL, tokens.get(1).type());
        assertEquals("world", tokens.get(1).lexeme());
    }

    @Test
    void tokenizesDecimalNumbers() {
        List<Token> tokens = tokenize("3.14");

        assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).type());
        assertEquals("3.14", tokens.get(0).lexeme());
    }

    @Test
    void recognizesPrintlnKeywordAndParens() {
        List<Token> tokens = tokenize("println(a);");

        List<TokenType> types = tokens.stream().map(Token::type).toList();
        assertEquals(
                List.of(
                        TokenType.PRINTLN,
                        TokenType.LEFT_PAREN,
                        TokenType.IDENTIFIER,
                        TokenType.RIGHT_PAREN,
                        TokenType.SEMICOLON,
                        TokenType.EOF),
                types);
    }

    @Test
    void tracksLineAndColumnAcrossNewlines() {
        List<Token> tokens = tokenize("let a: number = 1;\nlet b: number = 2;");

        Token secondLet = tokens.get(7);
        assertEquals(TokenType.LET, secondLet.type());
        assertEquals(2, secondLet.start().line());
        assertEquals(1, secondLet.start().column());
    }

    @Test
    void throwsOnUnterminatedString() {
        assertThrows(LexicalException.class, () -> tokenize("\"unterminated"));
    }

    @Test
    void throwsOnUnexpectedCharacter() {
        assertThrows(LexicalException.class, () -> tokenize("let x: number = 1 % 2;"));
    }

    @Test
    void recognizesExtensionOperatorsWhenRegistered() {
        PrintScriptLexer lexerWithPlugin = new PrintScriptLexer(Set.of(stubModuloOperator()));

        List<Token> tokens = drain(lexerWithPlugin.tokenize(new StringPositionalSource("1 % 2")));

        assertEquals(TokenType.EXTENSION_OPERATOR, tokens.get(1).type());
        assertEquals("%", tokens.get(1).lexeme());
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
