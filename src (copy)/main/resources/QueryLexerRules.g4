lexer grammar QueryLexerRules;

import CommonLexerRules;

/** Lexical grammar specific to query language grammar rules */
IN: 'in';
WS: F_WS -> skip; // throw away whitespace
REGEX: 'regex';
SEQ: 'seq';

