lexer grammar LogEntryLexerRules;

import CommonLexerRules;

LOG_ENTRY: NEWLINE LBRACK F_WS+ log_level_expr F_WS+ COMMA F_WS+ RBRACK F_WS COLON .;
LOG_LEVEL: [A-Z]+;
THREAD_NAME: [a-zA-Z0-9-]+;
LOG_TIMESTAMP:

log_level_expr: (F_UPPERCASE_CHAR)+;
