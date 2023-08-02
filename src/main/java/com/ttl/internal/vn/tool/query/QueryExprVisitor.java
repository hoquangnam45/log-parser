package com.ttl.internal.vn.tool.query;

import com.ttl.internal.vn.tool.grammar.QueryRulesBaseVisitor;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.ArrayAccessorExprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.Duration_const_exprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.ElementAccessorExprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.Query_sources_exprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.SwitchCaseQueryExprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.SwitchCaseSubExprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.TernaryQueryExprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.TernarySubExprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.Time_const_exprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser.TrivialQueryExprContext;
import com.ttl.internal.vn.tool.grammar.QueryRulesVisitor;
import com.ttl.internal.vn.tool.log.EmptyLogSource;
import com.ttl.internal.vn.tool.log.FileLogSource;
import com.ttl.internal.vn.tool.log.FilteredLogSource;
import com.ttl.internal.vn.tool.log.ILogSource;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class QueryExprVisitor extends QueryRulesBaseVisitor<Object> implements QueryRulesVisitor<Object>
{
	private final        QueryEvaluator          evaluator;
	private static final Map<String, ILogSource> logSourceMap = new HashMap<>();

	public QueryExprVisitor()
	{
		this.evaluator = new QueryEvaluator();
	}

	private ILogSource getSource(String path)
	{
		File file = new File(path);

		ILogSource logSource = logSourceMap.computeIfAbsent(file.getAbsolutePath(), absolutePath -> new FileLogSource(new File(absolutePath)));

		logSource.refresh();

		return logSource;
	}

	@Override
	public ILogSource visitFull_query_expr(QueryRulesParser.Full_query_exprContext ctx)
	{
		return Optional.of(ctx.query_sources_expr())
				.map(this::visitQuery_sources_expr)
				.map(source -> StreamSupport.stream(source.spliterator(), false)
						.filter(it -> {
								evaluator.setCurrentEntry(it);
								return (Boolean) visit(ctx.query_expr());
						})
				).map(FilteredLogSource::new)
				.map(ILogSource.class::cast)
				.orElseGet(EmptyLogSource::new);
	}

	@Override
	public ILogSource visitQuery_source_expr(QueryRulesParser.Query_source_exprContext ctx)
	{
		if (ctx.STRING() != null)
		{
			return getSource(ctx.STRING().getText());
		}
		if (ctx.constant_expr() != null)
		{
			return getSource(evaluator.constant(ctx.constant_expr().ID().getText(), String.class));
		}
		if (ctx.named_variable_expr() != null)
		{
			return getSource(evaluator.constant(ctx.named_variable_expr().ID().getText(), String.class));
		}
		if (ctx.full_query_expr() != null)
		{
			return visitQuery_sources_expr(ctx.full_query_expr().query_sources_expr());
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public ILogSource visitQuery_sources_expr(Query_sources_exprContext ctx)
	{
		return ctx.query_source_expr().stream().map(this::visitQuery_source_expr).reduce(new EmptyLogSource(), (acc, val) -> acc.add(val));
	}

	@Override
	public Object visitLiteral_expr(QueryRulesParser.Literal_exprContext ctx)
	{
		if (ctx.NULL() != null)
		{
			return null;
		}
		if (ctx.NUMBER() != null)
		{
			return new BigDecimal(ctx.NUMBER().getText());
		}
		if (ctx.duration_const_expr() != null)
		{
			return visitDuration_const_expr(ctx.duration_const_expr());
		}
		if (ctx.time_const_expr() != null)
		{
			return visitTime_const_expr(ctx.time_const_expr());
		}
		if (ctx.STRING() != null)
		{
			return ctx.STRING().getText();
		}
		if (ctx.BOOL() != null)
		{
			return Boolean.valueOf(ctx.BOOL().getText());
		}
		throw new UnsupportedOperationException("TypeError: " + ctx.getText());
	}

	@Override
	public Object visitFunc_expr(QueryRulesParser.Func_exprContext ctx)
	{
		return evaluator.evaluate(ctx.ID().getText(), visitElements_expr(ctx.elements_expr()).toArray());
	}

	@Override
	public List<Object> visitElements_expr(QueryRulesParser.Elements_exprContext ctx)
	{
		return ctx.element_expr().stream().map(this::visitElement_expr).collect(Collectors.toList());
	}

	@Override
	public Object visitElement_expr(QueryRulesParser.Element_exprContext ctx)
	{
		if (ctx.sub_expr() != null)
		{
			return visit(ctx.sub_expr());
		}
		if (ctx.query_expr() != null)
		{
			return visit(ctx.query_expr());
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitConstant_expr(QueryRulesParser.Constant_exprContext ctx)
	{
		return evaluator.constant(ctx.ID().getText());
	}

	@Override
	public Object visitNamed_variable_expr(QueryRulesParser.Named_variable_exprContext ctx)
	{
		return evaluator.variable(ctx.ID().getText());
	}

	@Override
	public Object visitTrivial_expr(QueryRulesParser.Trivial_exprContext ctx)
	{
		if (ctx.constant_expr() != null)
		{
			return visitConstant_expr(ctx.constant_expr());
		}
		if (ctx.literal_expr() != null)
		{
			return visitLiteral_expr(ctx.literal_expr());
		}
		if (ctx.named_variable_expr() != null)
		{
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
		return Optional.ofNullable(pattern).map(it -> OffsetDateTime.parse(val, DateTimeFormatter.ofPattern(it))).orElse(OffsetDateTime.parse(val));
	}

	@Override
	public Duration visitDuration_const_expr(Duration_const_exprContext ctx)
	{
		ChronoUnit unit = Optional.ofNullable(ctx.DURATION_UNIT()).map(TerminalNode::getText).map(it -> {
			switch (it)
			{
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
		}).orElse(ChronoUnit.SECONDS);
		return Duration.of(Long.parseLong(ctx.INT().getText()), unit);
	}

	@Override
	public Boolean visitGroupedQueryExpr(QueryRulesParser.GroupedQueryExprContext ctx)
	{
		return (boolean) visit(ctx.query_expr());
	}

	@Override
	public Boolean visitBoolLiteralExpr(QueryRulesParser.BoolLiteralExprContext ctx)
	{
		return Boolean.valueOf(ctx.BOOL().getText());
	}

	@Override
	public Boolean visitAndExpr(QueryRulesParser.AndExprContext ctx)
	{
		return (boolean) visit(ctx.query_expr(0)) && (boolean) visit(ctx.query_expr(1));
	}

	@Override
	public Object visitTrivialQueryExpr(TrivialQueryExprContext ctx)
	{
		return visitTrivial_expr(ctx.trivial_expr());
	}

	@Override
	public Boolean visitRegexExpr(QueryRulesParser.RegexExprContext ctx)
	{
		Pattern pattern = Pattern.compile((String) visit(ctx.sub_expr(0)));
		String val = (String) visit(ctx.sub_expr(1));
		Matcher matcher = pattern.matcher(val);
		return matcher.find();
	}

	@Override
	public Boolean visitComparisonExpr(QueryRulesParser.ComparisonExprContext ctx)
	{
		if (ctx.EQUAL() != null)
		{
			return evaluator.evaluate("==", Boolean.class, visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
		}
		if (ctx.LARGER() != null)
		{
			return evaluator.evaluate(">", Boolean.class, visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
		}
		if (ctx.LARGER_OR_EQUAL() != null)
		{
			return evaluator.evaluate(">=", Boolean.class, visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
		}
		if (ctx.SMALLER() != null)
		{
			return evaluator.evaluate("<", Boolean.class, visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
		}
		if (ctx.SMALLER_OR_EQUAL() != null)
		{
			return evaluator.evaluate("<=", Boolean.class, visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
		}
		if (ctx.UNEQUAL() != null)
		{
			return (Boolean) evaluator.evaluate("!=", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
		}
		throw new UnsupportedOperationException("Unsupported compare expression " + ctx.getText());
	}

	@Override
	public Object visitSwitchCaseQueryExpr(SwitchCaseQueryExprContext ctx)
	{
		for (int i = 0; i < ctx.query_expr().size() - 1; i += 2)
		{
			if ((Boolean) visit(ctx.query_expr(i)))
			{
				return visit(ctx.query_expr(i + 1));
			}
		}
		// Evaluate default expression
		return visit(ctx.query_expr(ctx.query_expr().size() - 1));
	}

	@Override
	public Object visitTernaryQueryExpr(TernaryQueryExprContext ctx)
	{
		if ((Boolean) visit(ctx.query_expr(0)))
		{
			return visit(ctx.query_expr(1));
		}
		return visit(ctx.query_expr(2));
	}

	@Override
	public Boolean visitInExpr(QueryRulesParser.InExprContext ctx)
	{
		Object val = visit(ctx.sub_expr(0));
		Object seq = visit(ctx.sub_expr(1));
		return Optional.ofNullable(seq).map(s -> {
			if (s instanceof String)
			{
				return Optional.ofNullable(val).map(v -> {
					if (v instanceof String)
					{
						return (String) v;
					}
					return null;
				}).map(v -> ((String) s).contains(v)).orElse(false);
			}
			if (s instanceof Iterable)
			{
				for (Object el : ((Iterable<?>) s))
				{
					if (el.equals(val))
					{
						return true;
					}
				}
				return false;
			}
			return false;
		}).orElse(false);
	}

	@Override
	public Boolean visitNotExpr(QueryRulesParser.NotExprContext ctx)
	{
		return !(Boolean) visit(ctx.query_expr());
	}

	@Override
	public Boolean visitOrExpr(QueryRulesParser.OrExprContext ctx)
	{
		return (Boolean) visit(ctx.query_expr(0)) || (Boolean) visit(ctx.query_expr(1));
	}

	@Override
	public Object visitGroupedSubExpr(QueryRulesParser.GroupedSubExprContext ctx)
	{
		return visit(ctx.sub_expr());
	}

	@Override
	public Object visitSwitchCaseSubExpr(SwitchCaseSubExprContext ctx)
	{
		for (int i = 0; i < ctx.query_expr().size() - 1; i++)
		{
			if ((Boolean) visit(ctx.query_expr(i)))
			{
				return visit(ctx.sub_expr(i));
			}
		}
		// Evaluate default expression
		return visit(ctx.sub_expr(ctx.sub_expr().size() - 1));
	}

	@Override
	public Object visitTernarySubExpr(TernarySubExprContext ctx)
	{
		if ((Boolean) visit(ctx.query_expr()))
		{
			return visit(ctx.sub_expr(1));
		}
		return visit(ctx.sub_expr(2));
	}

	@Override
	public Number visitMulDivExpr(QueryRulesParser.MulDivExprContext ctx)
	{
		if (ctx.MUL() != null)
		{
			return (Number) evaluator.evaluate("*", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
		}
		if (ctx.DIV() != null)
		{
			return (Number) evaluator.evaluate("/", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
		}
		if (ctx.MOD() != null)
		{
			return (Number) evaluator.evaluate("%", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
		}
		throw new UnsupportedOperationException("Do not support expression: " + ctx.getText());
	}

	@Override
	public Object visitElementAccessorExpr(ElementAccessorExprContext ctx)
	{
		Object val = visit(ctx.sub_expr());
		return Stream.of(Optional.ofNullable(val).map(Object::getClass).map(Class::getMethods).orElseGet(() -> new Method[] {})).filter(method -> Modifier.isPublic(method.getModifiers())).filter(method -> method.getParameterCount() == 0).filter(method -> method.getReturnType() != Void.class).filter(method -> method.getName().equals("get" + ctx.ID().getText().substring(0, 1).toUpperCase() + ctx.ID().getText().substring(1))) // NOTE: Check if this method is getter
				.findFirst().map(method -> {
					try
					{
						return method.invoke(val);
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
				});
	}

	@Override
	public List<Object> visitSeqExpr(QueryRulesParser.SeqExprContext ctx)
	{
		return visitElements_expr(ctx.elements_expr());
	}

	@Override
	public Object visitTrivialExpr(QueryRulesParser.TrivialExprContext ctx)
	{
		return visitTrivial_expr(ctx.trivial_expr());
	}

	@Override
	public Number visitExpExpr(QueryRulesParser.ExpExprContext ctx)
	{
		return (Number) evaluator.evaluate("exp", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
	}

	@Override
	public Object visitFuncExpr(QueryRulesParser.FuncExprContext ctx)
	{
		return visitFunc_expr(ctx.func_expr());
	}

	@Override
	public Object visitAddSubExpr(QueryRulesParser.AddSubExprContext ctx)
	{
		if (ctx.ADD() != null)
		{
			return evaluator.evaluate("+", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
		}
		if (ctx.SUB() != null)
		{
			return evaluator.evaluate("-", visit(ctx.sub_expr(0)), visit(ctx.sub_expr(1)));
		}
		throw new UnsupportedOperationException("Do not supported expression: " + ctx.getText());
	}

	@Override
	public Object visitArrayAccessorExpr(ArrayAccessorExprContext ctx)
	{
		Object val = visit(ctx.sub_expr(0));
		if (val.getClass().isArray())
		{
			Object[] arr = (Object[]) val;
			return arr[(int) visit(ctx.sub_expr(1))];
		}
		if (val instanceof List)
		{
			return ((List<?>) val).get((int) visit(ctx.sub_expr(1)));
		}
		if (val instanceof Map)
		{
			return ((Map<?, ?>) val).get(visit(ctx.sub_expr(1)));
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitErrorNode(ErrorNode errorNode)
	{
		throw new UnsupportedOperationException("Query is ill-formed and cannot be processed further, the error is here: " + errorNode.getText());
	}
}
