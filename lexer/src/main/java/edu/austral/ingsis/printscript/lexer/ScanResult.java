package edu.austral.ingsis.printscript.lexer;

import edu.austral.ingsis.printscript.common.Token;

record ScanResult(Token token, Cursor next) {}
