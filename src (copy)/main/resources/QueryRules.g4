/**
 * Documentation on how to define the grammar to parse the language
 * The definitive Antlr4 reference
 */
grammar QueryRules;

import QueryLexerRules;

/** The start rule; begin parsing here. */
prog: stat;
stat: expr;

/** Different types of expressions supported by the query language */
// Regex matching expressions
regex_expr: REGEX LPAREN ID COMMA STRING RPAREN;

// Literal expressions
time_literal_expr: TIME LPAREN STRING (COMMA STRING)? RPAREN;
duration_literal_expr: TIME LPAREN STRING (COMMA STRING)? RPAREN;
literal_expr:   STRING |
                NUMBER |
                NULL |
                BOOL |
                time_literal_expr |
                duration_literal_expr;

// The backbone of all expressions
expr:           literal_expr #LiteralExpr |
                LPAREN expr RPAREN #GroupingExpr | // Grouping expressions will have higher precedence
                SEQ LPAREN seq_elements? RPAREN #SeqExpr | // Expressions that defined a sequence of values
                ID LPAREN seq_elements? RPAREN #FuncExpr | // Expressions that defined a call to some external function
                regex_expr #RegexExpr | // Expressions that run the regex matching function
                <assoc=right> expr EXP expr #ExpExpr |
                expr (MUL | DIV) expr #MulDivExpr |
                expr (ADD | SUB) expr #AddSubExpr |
                expr AND expr #AndExpr |
                expr OR expr #OrExpr |
                (NOT | EXCLAMATION_MARK) expr #NotExpr |
                expr IN expr #InExpr |
                IF expr THEN expr #IfThenExpr |
                IF expr THEN expr ELSE expr #IfThenElseExpr |
                expr QM expr COLON expr #TernaryExpr |
                ID #IdExpr; // Named variables, constants, placeholders
seq_elements: seq_element (COMMA seq_element)*;
seq_element: expr;
