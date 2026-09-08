package edu.austral.ingsis.printscript.parser;

import edu.austral.ingsis.printscript.common.TokenStream;

/**
 * What parsing one thing produces: the thing itself, plus the {@link TokenStream} that's left over
 * after consuming it. Same idea as the lexer's {@code ScanResult} — a pure "here's what I built,
 * and here's where to continue from" pair, instead of a shared cursor that mutates as it goes.
 */
record ParseResult<T>(T node, TokenStream rest) {}
