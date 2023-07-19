lexer grammar CommonLexerRules;

// Tokens will be matched to longest matched lexical rule, if multiple rule satisfy the token
// the chosen lexical rule will be chosen by top-down order in the lexical grammar
// Ordering is important

// The supported tokens that can be used in the parser rules
NUMBER: F_INT | F_FLOAT;
BOOL: F_TRUE | F_FALSE;
ID: [a-zA-Z][a-zA-Z0-9]+; // ID do not allow to start withs number
EXCLAMATION_MARK: '!';
DOUBLE_EQUAL: '==';
UNEQUAL: '!=';
EQUAL: '=';
NOT: 'not';
TILDE: '~';
NULL: 'null' ;
LINE_COMMENT : '//' .*? '\r'? '\n' ; // Match "//" stuff '\n'
COMMENT : '/*' .*? '*/' ; // Match "/*" stuff "*/"
NEWLINE: '\r'? '\n' ; // return newlines to parser (is end-statement signal)
TIME: 'time';
IF: 'if';
THEN: 'then';
ELSE: 'else';
COLON: ':';
LPAREN: '(';
RPAREN: ')';
LBRACK: '[';
RBRACK: ']';
COMMA: ',';
QM: '?';
MUL: '*';
DIV: '/';
MOD: '%';
ADD: '+';
SUB: '-';
AND: 'and' | '&&';
EXP: '^';
OR: 'or' | '||';
FOR: 'for';
INC: '++';
DEC: '--';
DURATION: 'duration';
STRING: '"' (F_ESC|.)*? '"' ; // String is defined with double quote, allow escaping special character

fragment F_INT: '-'? F_DIGIT+;
fragment F_FLOAT: '-'? (F_DIGIT+ ('.' F_DIGIT*)?);
fragment F_DIGIT: [0-9];
fragment F_WS : [ \t\r\n]+; // match 1-or-more whitespace but discard
fragment F_ESC : '\\"' | '\\\\' ; // 2-char sequences \" and \\
fragment F_FALSE: 'false';
fragment F_TRUE: 'true';

