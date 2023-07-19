/**
 * Documentation on how to define the grammar to parse the language
 * The definitive Antlr4 reference
 */
grammar QueryRules;

import QueryLexerRules;

/**
 * The start rule, begin parsing here.
 * Ordering of the expressions in the grammar file is important
 * Specification:
 *   - Types:
 *      + Number
 *      + Boolean
 *      + String: defined with with double quotes
 *      + Timestamp: defined as string enclosed in the function time(str, format), format is optional
 *      + Duration: defined as number enclosed in the function duration(num, unit), unit is optional and default to seconds
 *
 * Example: Basic query: in('ABC.log') filter threadName = "thread#1" and timeStamp > now - duration(3, 'sec')
 */
full_query_expr: IN LPAREN STRING RPAREN FILTER (branchable_query_expr | query_expr);

/** Different types of expressions supported by the query language */
// Literal expressions
time_literal_expr: TIME LPAREN STRING (COMMA STRING)? RPAREN;
duration_literal_expr: TIME LPAREN INT (COMMA DURATION_UNIT)? RPAREN;
literal_expr:   STRING |
                NUMBER |
                NULL |
                BOOL |
                time_literal_expr |
                duration_literal_expr
                ;

// Regex matching expressions
regex_expr: REGEX LPAREN sub_expr COMMA STRING RPAREN;

// Branchable query expressions
branchable_query_expr:  SWITCH CASE query_expr COLON query_expr (CASE query_expr COLON query_expr)* DEFAULT COLON query_expr #SwitchCaseExpr |
                        query_expr QM query_expr COLON query_expr #TernaryExpr
                        ;

// Expressions that is the main expression of the query language
query_expr:     BOOL #BoolLiteralExpr |
                regex_expr #RegexExpr |
                LPAREN query_expr RPAREN #GroupExpr | // Grouping expressions will have higher precedence
                sub_expr (LARGER | LARGER_OR_EQUAL | SMALLER_OR_EQUAL | SMALLER | EQUAL | UNEQUAL) sub_expr #CompareExpr |
                query_expr AND query_expr #AndExpr |
                query_expr OR query_expr #OrExpr |
                sub_expr IN sub_expr #InExpr |
                (NOT | EXCLAMATION_MARK) query_expr #NotExpr
                ;

// The backbone of all expressions - sub expressions
sub_expr:       literal_expr #LiteralExpr |
                SEQ LPAREN seq_elements? RPAREN #SeqExpr | // Expressions that defined a sequence of values
                ID LPAREN seq_elements? RPAREN #FuncExpr | // Expressions that defined a call to some external function
                <assoc=right> sub_expr EXP sub_expr #ExpExpr |
                sub_expr (MUL | DIV) sub_expr #MulDivExpr |
                sub_expr (ADD | SUB) sub_expr #AddSubExpr |
                ID #IdExpr // Named variables, constants
                ;
seq_elements: seq_element (COMMA seq_element)*;
seq_element: sub_expr;
