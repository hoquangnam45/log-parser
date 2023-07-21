lexer grammar QueryLexerRules;

import CommonLexerRules;

/** Lexical grammar specific to query language grammar rules */
IN: 'in';
WS: F_WS -> skip; // throw away whitespace
REGEX: 'regex';
SEQ: 'seq';
FILTER: 'filter';
LARGER_OR_EQUAL: '>=';
SMALLER_OR_EQUAL: '<=';
LARGER: '>';
SMALLER: '<';
DURATION_UNIT: 'sec' | 's' | 'min' | 'm' | 'hour' | 'h' | 'day' | 'd' | 'mon' | 'year' | 'y' | 'ms' | 'us';
SWITCH: 'switch';
CASE: 'case';
DEFAULT: 'default';
AT: '@';
SHARP: '#';



