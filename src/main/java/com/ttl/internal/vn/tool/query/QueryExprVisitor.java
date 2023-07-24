package com.ttl.internal.vn.tool.query;

import com.ttl.internal.vn.tool.grammar.QueryRulesBaseVisitor;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.Duration_const_exprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.SwitchCaseQueryExprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.SwitchCaseSubExprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.TertiaryQueryExprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.TertiarySubExprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.Time_const_exprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.TrivialQueryExprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesVisitor;
import com.ttl.internal.vn.tool.log.FilteredLogSource;
import com.ttl.internal.vn.tool.log.ILogEntry;
import com.ttl.internal.vn.tool.log.ILogSource;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.File;
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
import java.util.stream.Stream;

//TODO: (Optimization) Cache query so it don't have to reevaluate it next time
public class QueryExprVisitor implements QueryRulesVisitor<Object> {
    private QueryEvaluator evaluator;

    private ILogSource getSource(String sourceId) {
        String path = sourceId;
        File file = new File(path);
        return null;
    }

    // This function depends on side-effects
    // TODO: Implement this
    @Override
    public ILogSource visitFull_query_expr(QueryRulesParser.Full_query_exprContext ctx) {
        ILogSource source = getSource(visitQuery_source_expr(ctx.query_source_expr()));
        Stream<ILogEntry> filteredEntry = source.map(QueryEvaluator::new)
                .filter(evaluator -> (Boolean) visit(ctx.query_expr()))
                .map(QueryEvaluator::getEntry);
        return new FilteredLogSource(filteredEntry);
    }

    @Override
    public String visitQuery_source_expr(QueryRulesParser.Query_source_exprContext ctx) {
        if (ctx.STRING() != null) {
            return ctx.STRING().getText();
        }
        if (ctx.constant_expr() != null) {
            return evaluator.constant(ctx.constant_expr().ID().getText(), String.class);
        }
        if (ctx.named_variable_expr() != null) {
            return evaluator.constant(ctx.named_variable_expr().ID().getText(), String.class);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitLiteral_expr(QueryRulesParser.Literal_exprContext ctx) {
        if (ctx.NULL() != null) {
            return null;
        }
        if (ctx.NUMBER() != null) {
            return new BigDecimal(ctx.NUMBER().getText());
        }
        if (ctx.duration_const_expr() != null) {
            return visitDuration_const_expr(ctx.duration_const_expr());
        }
        if (ctx.time_const_expr() != null) {
            return visitTime_const_expr(ctx.time_const_expr());
        }
        if (ctx.STRING() != null) {
            return ctx.STRING().getText();
        }
        if (ctx.BOOL() != null) {
            return Boolean.valueOf(ctx.BOOL().getText());
        }
        throw new UnsupportedOperationException("TypeError: " + ctx.getText());
    }

    @Override
    public Object visitFunc_expr(QueryRulesParser.Func_exprContext ctx) {
        return evaluator.evaluate(ctx.ID().getText(), visitElements_expr(ctx.elements_expr()).toArray());
    }

    @Override
    public List<Object> visitElements_expr(QueryRulesParser.Elements_exprContext ctx) {
        return ctx.element_expr().stream().map(this::visitElement_expr).collect(Collectors.toList());
    }

    @Override
    public Object visitElement_expr(QueryRulesParser.Element_exprContext ctx) {
        if (ctx.sub_expr() != null) {
            return visit(ctx.sub_expr());
        }
        if (ctx.seq_expr() != null) {
            return visitSeq_expr(ctx.seq_expr());
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean visitRegex_expr(QueryRulesParser.Regex_exprContext ctx) {
        Pattern pattern = Pattern.compile(visitRegex_param_expr(ctx.regex_param_expr(1)));
        String val = visitRegex_param_expr(ctx.regex_param_expr(0));
        Matcher matcher = pattern.matcher(val);
        return matcher.find();
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
        if (ctx.constant_expr() != null) {
            return visitConstant_expr(ctx.constant_expr());
        }
        if (ctx.literal_expr() != null) {
            return visitLiteral_expr(ctx.literal_expr());
        }
        if (ctx.named_variable_expr() != null) {
            return visitNamed_variable_expr(ctx.named_variable_expr());
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public OffsetDateTime visitTime_const_expr(Time_const_exprContext ctx)
    {
        int numberOfParameters = ctx.STRING().size();
        String pattern = numberOfParameters == 2 ? ctx.STRING(1).getText() : null;
        String val = ctx.STRING(0).getText();
        return Optional.ofNullable(pattern)
                .map(it -> OffsetDateTime.parse(val, DateTimeFormatter.ofPattern(it)))
                .orElse(OffsetDateTime.parse(val));
    }

    @Override
    public Duration visitDuration_const_expr(Duration_const_exprContext ctx)
    {
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
    public Boolean visitTernaryExpr(QueryRulesParser.TernaryExprContext ctx) {
        if ((Boolean) visit(ctx.grouped_query_expr(0))) {
            return visit(ctx.grouped_query_expr(1));
        }
        return visit(ctx.grouped_query_expr(2));
    }

    @Override
    public Boolean visitGroupedQueryExpr(QueryRulesParser.GroupedQueryExprContext ctx) {
        return (boolean) visit(ctx.query_expr());
    }

    @Override
    public Boolean visitQueryExpr(QueryRulesParser.QueryExprContext ctx) {
        return (boolean) visit(ctx.query_expr());
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
    public Object visitTrivialQueryExpr(TrivialQueryExprContext ctx)
    {
        return visitTrivial_expr(ctx.trivial_expr());
    }

    @Override
    public Boolean visitRegexExpr(QueryRulesParser.RegexExprContext ctx) {
        Pattern pattern = Pattern.compile((String) visit(ctx.sub_expr(0)));
        String val = (String) visit(ctx.sub_expr(1));
        Matcher matcher = pattern.matcher(val);
        return matcher.find();
    }

    @Override
    public Boolean visitComparisonExpr(QueryRulesParser.ComparisonExprContext ctx) {
        if (ctx.EQUAL() != null) {
            return evaluator.evaluate("==", Boolean.class, visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.LARGER() != null) {
            return evaluator.evaluate(">", Boolean.class, visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.LARGER_OR_EQUAL() != null) {
            return evaluator.evaluate(">=", Boolean.class, visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.SMALLER() != null) {
            return evaluator.evaluate("<", Boolean.class, visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.SMALLER_OR_EQUAL() != null) {
            return evaluator.evaluate("<=", Boolean.class, visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.UNEQUAL() != null) {
            return (Boolean) evaluator.evaluate("!=", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        throw new UnsupportedOperationException("Unsupported compare expression " + ctx.getText());
    }

    @Override
    public Object visitSwitchCaseQueryExpr(SwitchCaseQueryExprContext ctx)
    {
        return null;
    }

    @Override
    public Object visitTertiaryQueryExpr(TertiaryQueryExprContext ctx)
    {
        return null;
    }

    @Override
    public Boolean visitInExpr(QueryRulesParser.InExprContext ctx) {
        Object val = visit(ctx.sub_expr());
        List<Object> seq = visitSeq_expr(ctx.seq_expr());
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
    public Object visitTertiarySubExpr(TertiarySubExprContext ctx)
    {
        return null;
    }

    @Override
    public Object visitGroupedSubExpr(QueryRulesParser.GroupedSubExprContext ctx) {
        return visit(ctx.sub_expr());
    }

    @Override
    public Object visitSwitchCaseSubExpr(SwitchCaseSubExprContext ctx)
    {
        for (int i = 0; i < ctx.sub_expr().size() - 1; i += 2) {
            if ((Boolean) visit(ctx.sub_expr(i))) {
                return visit(ctx.sub_expr(i + 1));
            }
        }
        // Evaluate default expression
        return visit(ctx.sub_expr(ctx.sub_expr().size() - 1));
    }

    @Override
    public Object visitSubExpr(QueryRulesParser.SubExprContext ctx) {
        return visit(ctx.sub_expr());
    }

    @Override
    public Number visitMulDivExpr(QueryRulesParser.MulDivExprContext ctx) {
        if (ctx.MUL() != null) {
            return (Number) evaluator.evaluate("*", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.DIV() != null) {
            return (Number) evaluator.evaluate("/", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        if (ctx.MOD() != null) {
            return (Number) evaluator.evaluate("%", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
        }
        throw new UnsupportedOperationException("Do not support expression: " + ctx.getText());
    }

    @Override
    public List<Object> visitSeqExpr(QueryRulesParser.SeqExprContext ctx) {
        return visitSeq_expr(ctx.());
    }

    @Override
    public Object visitTrivialExpr(QueryRulesParser.TrivialExprContext ctx) {
        return null;
    }

    @Override
    public Number visitExpExpr(QueryRulesParser.ExpExprContext ctx) {
        return (Number) evaluator.evaluate("exp", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
    }

    @Override
    public Object visitFuncExpr(QueryRulesParser.FuncExprContext ctx) {
        return visitFunc_expr(ctx.func_expr());
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
