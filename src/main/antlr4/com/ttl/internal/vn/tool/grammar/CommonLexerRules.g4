lexer grammar CommonLexerRules;

// Tokens will be matched to longest matched lexical rule regardless whether that lexical rule
// appear anywhere in the parser rule or not, if multiple lexical rule satisfy the token
// the chosen lexical rule will be chosen by top-down order in the lexical grammar file.
// The ordering of the rule is important

// The supported tokens that can be used in the parser rules
NUMBER: INT | FLOAT;
INT: '-'? F_DIGIT+;
FLOAT: '-'? (F_DIGIT+ ('.' F_DIGIT*)?);
BOOL: F_TRUE | F_FALSE;
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
ELSEIF: 'elif';
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
DOT: '.';

fragment F_ID: (F_LOWERCASE_CHAR | F_UPPERCASE_CHAR)(F_LOWERCASE_CHAR | F_UPPERCASE_CHAR | F_DIGIT)+; // ID do not allow to start withs number - Lowest priority
fragment F_LOWERCASE_CHAR: [a-z];
fragment F_UPPERCASE_CHAR: [A-Z];
fragment F_DIGIT: [0-9];
fragment F_WS : [ \t\r\n]; // match 1-or-more whitespace but discard
fragment F_ESC : '\\"' | '\\\\' ; // 2-char sequences \" and \\
fragment F_FALSE: 'false';
fragment F_TRUE: 'true';

