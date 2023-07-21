/**
 * Documentation on how to define the grammar to parse the language
 * The definitive Antlr4 reference
 */
grammar QueryRules;

import QueryLexerRules;

/**
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

// The start rule, begin parsing here.
full_query_expr: IN LPAREN (STRING | constant_expr | named_variable_expr) RPAREN FILTER (branchable_query_expr | query_expr) EOF;

/** Different types of expressions supported by the query language */
// Branchable query expressions
branchable_query_expr:  SWITCH CASE grouped_query_expr COLON grouped_query_expr (CASE grouped_query_expr COLON grouped_query_expr)* DEFAULT COLON grouped_query_expr #SwitchCaseExpr |
                        grouped_query_expr QM grouped_query_expr COLON grouped_query_expr #TernaryExpr
                        ;

// Grouped query expressions
grouped_query_expr: LPAREN query_expr RPAREN |
                    query_expr;

// Expressions that is the main expression of the query language
query_expr:     BOOL |
                regex_expr |
                grouped_sub_expr (LARGER | LARGER_OR_EQUAL | SMALLER_OR_EQUAL | SMALLER | EQUAL | UNEQUAL) grouped_sub_expr |
                query_expr AND query_expr |
                query_expr OR query_expr |
                seq_expr IN seq_expr |
                (NOT | EXCLAMATION_MARK) query_expr
                ;

// Sub-expressions - the backbone expressions
// Grouped sub expressions
grouped_sub_expr: LPAREN sub_expr RPAREN |
                  sub_expr
                  ;
sub_expr:   <assoc=right> sub_expr EXP sub_expr |
            sub_expr (MUL | DIV | MOD) sub_expr |
            sub_expr (ADD | SUB) sub_expr |
            seq_expr |
            join_seq_expr |
            regex_expr |
            func_expr |
            trivial_expr
            ;

seq_expr:   SEQ LPAREN elements_expr? RPAREN | func_expr;
join_seq_expr: seq_expr ADD seq_expr;

regex_expr: REGEX LPAREN regex_param_expr COMMA regex_param_expr RPAREN;
regex_param_expr: STRING | constant_expr | named_variable_expr | func_expr;

constant_expr: AT ID;
named_variable_expr: SHARP ID;
trivial_expr:   literal_expr |
                constant_expr |
                named_variable_expr
                ;

time_literal_expr: TIME LPAREN STRING (COMMA STRING)? RPAREN;
duration_literal_expr: TIME LPAREN INT (COMMA DURATION_UNIT)? RPAREN;

literal_expr:   STRING |
                NUMBER |
                NULL |
                BOOL |
                time_literal_expr |
                duration_literal_expr
                ;

func_expr: ID LPAREN elements_expr? RPAREN;
elements_expr: element_expr (COMMA element_expr)*;
element_expr: sub_expr | seq_expr; // Expressions that defined a call to some external function