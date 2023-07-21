package com.ttl.internal.vn.tool.query;

import com.ttl.internal.vn.tool.grammar.QueryRulesBaseVisitor;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser;
import com.ttl.internal.vn.tool.grammar.QueryRulesVisitor;
import com.ttl.internal.vn.tool.log.LogEntry;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//TODO: (Optimization) Cache query so it don't have to reparse it next time
public class QueryExprVisitor implements QueryRulesVisitor<Object> {
    private final QueryEvaluator evaluator = QueryEvaluator.getInstance();

    // This function depends on side-effects
    // TODO: Implement this
    @Override
    public List<Object> visitFull_query_expr(QueryRulesParser.Full_query_exprContext ctx) {
//        String source;
//        // Construct source from string <- side-effect happened here
//        // Construct predicate from query
//        Object query = vi
//        Predicate<LogEntry> predicate = entry -> {
//
//        } predicate
//        // Iterate over log entry
//        // Filter log entry that match predicate;
//        return List.of();
        return List.of();
    }

    @Override
    public OffsetDateTime visitTime_literal_expr(QueryRulesParser.Time_literal_exprContext ctx) {
        int numberOfParameters = ctx.STRING().size();
        String pattern = numberOfParameters == 2 ? ctx.STRING(1).getText() : null;
        String val = ctx.STRING(0).getText();
        return Optional.ofNullable(pattern)
                .map(it -> OffsetDateTime.parse(val, DateTimeFormatter.ofPattern(it)))
                .orElse(OffsetDateTime.parse(val));
    }

    @Override
    public Duration visitDuration_literal_expr(QueryRulesParser.Duration_literal_exprContext ctx) {
        ChronoUnit unit = Optional.ofNullable(ctx.DURATION_UNIT())
                .map(TerminalNode::getText)
                .map(it -> {
                    switch (it) {
                        case "sec":
                        case "s":
                            return ChronoUnit.SECONDS;
                        case "min":
                        case "m":
                            return ChronoUnit.MINUTES;
                        case "hour":
                        case "h":
                            return ChronoUnit.HOURS;
                        case "day":
                        case "d":
                            return ChronoUnit.DAYS;
                        case "mon":
                            return ChronoUnit.MONTHS;
                        case "year":
                        case "y":
                            return ChronoUnit.YEARS;
                        case "ms":
                            return ChronoUnit.MILLIS;
                        case "us":
                            return ChronoUnit.MICROS;
                        default:
                            throw new UnsupportedOperationException("Duration unit is missing " + it);
                    }
                })
                .orElse(ChronoUnit.SECONDS);
        return Duration.of(Long.parseLong(ctx.INT().getText()), unit);
    }

    @Override
    public Object visitLiteral_expr(QueryRulesParser.Literal_exprContext ctx) {
        if (ctx.NULL() != null) {
            return null;
        }
        if (ctx.NUMBER() != null) {
            return new BigDecimal(ctx.NUMBER().getText());
        }
        if (ctx.duration_literal_expr() != null) {
            return visitDuration_literal_expr(ctx.duration_literal_expr());
        }
        if (ctx.time_literal_expr() != null) {
            return visitTime_literal_expr(ctx.time_literal_expr());
        }
        if (ctx.STRING() != null) {
            return ctx.STRING().getText();
        }
        throw new UnsupportedOperationException("TypeError: " + ctx.getText());
    }

    @Override
    public Object visitFunc_expr(QueryRulesParser.Func_exprContext ctx) {
        return null;
    }

    @Override
    public List<Object> visitElements_expr(QueryRulesParser.Elements_exprContext ctx) {
        return ctx.element_expr().stream().map(this::visitElement_expr).collect(Collectors.toList());
    }

    @Override
    public Object visitElement_expr(QueryRulesParser.Element_exprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitRegex_expr(QueryRulesParser.Regex_exprContext ctx) {
        Pattern pattern = Pattern.compile((String) visit(ctx.regex_param_expr(1)));
        String val = (String) visit(ctx.regex_param_expr(0));
        Matcher matcher = pattern.matcher(val);
        return matcher.find();
    }

    @Override
    public String visitRegex_param_expr(QueryRulesParser.Regex_param_exprContext ctx) {
        if (ctx.STRING() != null) {
            return ctx.STRING().getText();
        }
        if (ctx.constant_expr() != null) {
            return evaluator.constant(ctx.constant_expr().ID().getText(), String.class);
        }
        if (ctx.named_variable_expr() != null) {
            return evaluator.constant(ctx.named_variable_expr().getText(), String.class);
        }
        if (ctx.func_expr() != null) {
            return (String) visit(ctx.func_expr());
        }
        throw new UnsupportedOperationException("Cannot evaluate expressions: " + ctx.getText());
    }

    @Override
    public Object visitConstant_expr(QueryRulesParser.Constant_exprContext ctx) {
        return evaluator.constant(ctx.ID().getText());
    }

    @Override
    public Object visitNamed_variable_expr(QueryRulesParser.Named_variable_exprContext ctx) {
        return evaluator.variable(ctx.ID().getText());
    }

    @Override
    public Object visitTrivial_expr(QueryRulesParser.Trivial_exprContext ctx) {
        return visit(ctx);
    }

    @Override
    public Object visitSwitchCaseExpr(QueryRulesParser.SwitchCaseExprContext ctx) {
        for (int i = 0; i < ctx.grouped_query_expr().size() - 1; i += 2) {
            if ((Boolean) visit(ctx.grouped_query_expr(i))) {
                return visit(ctx.grouped_query_expr(i + 1));
            }
        }
        // Evaluate default expression
        return visit(ctx.grouped_query_expr(ctx.grouped_query_expr().size() - 1));
    }

    @Override
    public Object visitTernaryExpr(QueryRulesParser.TernaryExprContext ctx) {
        if ((Boolean) visit(ctx.grouped_query_expr(0))) {
            return visit(ctx.grouped_query_expr(1));
        }
        return visit(ctx.grouped_query_expr(2));
    }

    @Override
    public Object visitGrouped_query_expr(QueryRulesParser.Grouped_query_exprContext ctx) {
        return null;
    }

    @Override
    public Object visitQuery_expr(QueryRulesParser.Query_exprContext ctx) {
        return null;
    }

    @Override
    public Object visitGrouped_sub_expr(QueryRulesParser.Grouped_sub_exprContext ctx) {
        return null;
    }

    @Override
    public Object visitSub_expr(QueryRulesParser.Sub_exprContext ctx) {
        return null;
    }

    @Override
    public Object visitSeq_expr(QueryRulesParser.Seq_exprContext ctx) {
        return null;
    }

    @Override
    public Object visitJoin_seq_expr(QueryRulesParser.Join_seq_exprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitBoolLiteralExpr(QueryRulesParser.BoolLiteralExprContext ctx) {
        return Boolean.valueOf(ctx.BOOL().getText());
    }

    @Override
    public Boolean visitAndExpr(QueryRulesParser.AndExprContext ctx) {
        return (boolean) visit(ctx.query_expr(0)) && (boolean) visit(ctx.query_expr(1));
    }

    @Override
    public Object visitGroupExpr(QueryRulesParser.GroupExprContext ctx) {
        return visit(ctx.query_expr());
    }

    @Override
    public Boolean visitRegexExpr(QueryRulesParser.RegexExprContext ctx) {
        return visitRegex_expr(ctx.regex_expr());
    }

    @Override
    public Boolean visitCompareExpr(QueryRulesParser.CompareExprContext ctx) {
        if (ctx.EQUAL() != null) {
            return (Boolean) evaluator.evaluate("==", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.LARGER() != null) {
            return (Boolean) evaluator.evaluate(">", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.LARGER_OR_EQUAL() != null) {
            return (Boolean) evaluator.evaluate(">=", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.SMALLER() != null) {
            return (Boolean) evaluator.evaluate("<", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.SMALLER_OR_EQUAL() != null) {
            return (Boolean) evaluator.evaluate("<=", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.UNEQUAL() != null) {
            return (Boolean) evaluator.evaluate("!=", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        throw new UnsupportedOperationException("Unsupported compare expression " + ctx.getText());
    }

    @Override
    public Boolean visitInExpr(QueryRulesParser.InExprContext ctx) {
        Object val = visit(ctx.sub_expr(0));
        Object seq = visit(ctx.sub_expr(1));
        return (Boolean) evaluator.evaluate("in", val, seq);
    }

    @Override
    public Boolean visitNotExpr(QueryRulesParser.NotExprContext ctx) {
        return (Boolean) evaluator.evaluate("not", visit(ctx.query_expr()));
    }

    @Override
    public Boolean visitOrExpr(QueryRulesParser.OrExprContext ctx) {
        return (Boolean) evaluator.evaluate("or", visit(ctx.query_expr(0)), visit(ctx.query_expr(1)));
    }

    @Override
    public Number visitMulDivExpr(QueryRulesParser.MulDivExprContext ctx) {
        if (ctx.MUL() != null) {
            return (Number) evaluator.evaluate("*", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.DIV() != null) {
            return (Number) evaluator.evaluate("/", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        throw new UnsupportedOperationException("Do not support expression: " + ctx.getText());
    }

    @Override
    public Number visitExpExpr(QueryRulesParser.ExpExprContext ctx) {
        return (Number) evaluator.evaluate("exp", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
    }

    @Override
    public Object visitFuncExpr(QueryRulesParser.FuncExprContext ctx) {
        return evaluator.evaluate(ctx.ID().getText(), visitSeq_elements(ctx.seq_elements()));
    }

    @Override
    public Object visitAddSubExpr(QueryRulesParser.AddSubExprContext ctx) {
        if (ctx.ADD() != null) {
            return evaluator.evaluate("+", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.SUB() != null) {
            return evaluator.evaluate("-", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        throw new UnsupportedOperationException("Do not supported expression: " + ctx.getText());
    }

    @Override
    public Object visit(ParseTree parseTree) {
        return null;
    }

    @Override
    public Object visitChildren(RuleNode ruleNode) {
        return null;
    }

    @Override
    public Object visitTerminal(TerminalNode terminalNode) {
        return null;
    }

    @Override
    public Void visitErrorNode(ErrorNode errorNode) {
        throw new UnsupportedOperationException("Query is ill-formed and cannot be processed further, the error is here: " + errorNode.getText());
    }
}
