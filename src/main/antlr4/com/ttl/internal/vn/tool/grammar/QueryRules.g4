/**
 * Documentation on how to define the grammar to parse the language
 * The definitive Antlr4 reference
 */
grammar QueryRules;

import QueryLexerRules;

/**
 * NOTE: Ordering of the expressions in the grammar file is important
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
full_query_expr: IN LPAREN query_sources_expr RPAREN FILTER query_expr EOF;
query_source_expr: STRING | constant_expr | named_variable_expr | full_query_expr;
query_sources_expr: query_source_expr (COMMA query_source_expr)*;

/** Different types of expressions supported by the query language */
// Expressions that is the main expression of the query language
query_expr:     SWITCH CASE query_expr COLON query_expr (CASE query_expr COLON query_expr)* DEFAULT COLON query_expr #SwitchCaseQueryExpr |
                LPAREN query_expr RPAREN QM query_expr COLON query_expr #TernaryQueryExpr |
                LPAREN query_expr RPAREN #GroupedQueryExpr |
                BOOL #BoolLiteralExpr |
                REGEX LPAREN sub_expr COMMA sub_expr RPAREN #RegexExpr |
                sub_expr (LARGER | LARGER_OR_EQUAL | SMALLER_OR_EQUAL | SMALLER | EQUAL | UNEQUAL) sub_expr #ComparisonExpr |
                query_expr AND query_expr #AndExpr |
                query_expr OR query_expr #OrExpr |
                sub_expr IN sub_expr #InExpr |
                (NOT | EXCLAMATION_MARK) query_expr #NotExpr |
                trivial_expr #TrivialQueryExpr
                ;

// Sub-expressions - the backbone expressions
sub_expr:   SWITCH CASE query_expr COLON sub_expr (CASE query_expr COLON sub_expr)* DEFAULT COLON sub_expr #SwitchCaseSubExpr |
            LPAREN query_expr RPAREN QM sub_expr COLON sub_expr #TernarySubExpr |
            LPAREN sub_expr RPAREN #GroupedSubExpr |
            sub_expr LBRACK sub_expr RBRACK #ArrayAccessorExpr |
            sub_expr DOT ID #ElementAccessorExpr |
            <assoc=right> sub_expr EXP sub_expr #ExpExpr |
            sub_expr (MUL | DIV | MOD) sub_expr #MulDivExpr |
            sub_expr (ADD | SUB) sub_expr #AddSubExpr |
            SEQ LPAREN elements_expr? RPAREN #SeqExpr |
            func_expr #FuncExpr |
            trivial_expr #TrivialExpr
            ;

constant_expr: AT ID;
named_variable_expr: SHARP ID;
trivial_expr:   literal_expr |
                constant_expr |
                named_variable_expr
                ;

time_const_expr: TIME LPAREN STRING (COMMA STRING)? RPAREN;
duration_const_expr: TIME LPAREN INT (COMMA DURATION_UNIT)? RPAREN;

literal_expr:   STRING |
                NUMBER |
                NULL |
                BOOL |
                time_const_expr |
                duration_const_expr
                ;

func_expr: ID LPAREN elements_expr? RPAREN;
elements_expr: element_expr (COMMA element_expr)*;
element_expr: sub_expr | query_expr; // Expressions that defined a call to some external function