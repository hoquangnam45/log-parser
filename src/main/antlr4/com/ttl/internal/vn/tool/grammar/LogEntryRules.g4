/**
 * Documentation on how to define the grammar to parse the language
 * The definitive Antlr4 reference
 */
grammar LogEntryRules;

import LogEntryLexerRules;

log_entry_expr: !(F_WS)+ LBRACK F_WS+ log_level_expr F_WS+ COMMA F_WS+ RBRACK F_WS COLON . NEWLINE;

log_level_expr: F_UPPERCA+;
