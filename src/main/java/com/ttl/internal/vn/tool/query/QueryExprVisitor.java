package com.ttl.internal.vn.tool.query;

import com.ttl.internal.vn.tool.grammar.QueryRulesParser;
import com.ttl.internal.vn.tool.grammar.QueryRulesVisitor;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class QueryExprVisitor implements QueryRulesVisitor<Boolean> {
    private QueryEvaluator evaluator = QueryEvaluator.getInstance();

    @Override
    public Boolean visitFull_query_expr(QueryRulesParser.Full_query_exprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitTime_literal_expr(QueryRulesParser.Time_literal_exprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitDuration_literal_expr(QueryRulesParser.Duration_literal_exprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitLiteral_expr(QueryRulesParser.Literal_exprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitRegex_expr(QueryRulesParser.Regex_exprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitIfElseExpr(QueryRulesParser.IfElseExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitTernaryExpr(QueryRulesParser.TernaryExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitSubBranchQueryExpr(QueryRulesParser.SubBranchQueryExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitBoolLiteralExpr(QueryRulesParser.BoolLiteralExprContext ctx) {
        return Boolean.valueOf(ctx.BOOL().getText());
    }

    @Override
    public Boolean visitAndExpr(QueryRulesParser.AndExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitGroupExpr(QueryRulesParser.GroupExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitRegexExpr(QueryRulesParser.RegexExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitCompareExpr(QueryRulesParser.CompareExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitSubExpr(QueryRulesParser.SubExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitInExpr(QueryRulesParser.InExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitNotExpr(QueryRulesParser.NotExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitOrExpr(QueryRulesParser.OrExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitMulDivExpr(QueryRulesParser.MulDivExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitIdExpr(QueryRulesParser.IdExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitLiteralExpr(QueryRulesParser.LiteralExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitSeqExpr(QueryRulesParser.SeqExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitExpExpr(QueryRulesParser.ExpExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitFuncExpr(QueryRulesParser.FuncExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitAddSubExpr(QueryRulesParser.AddSubExprContext ctx) {
        return null;
    }

    @Override
    public Boolean visitSeq_elements(QueryRulesParser.Seq_elementsContext ctx) {
        return null;
    }

    @Override
    public Boolean visitSeq_element(QueryRulesParser.Seq_elementContext ctx) {
        return null;
    }

    @Override
    public Boolean visit(ParseTree parseTree) {
        return null;
    }

    @Override
    public Boolean visitChildren(RuleNode ruleNode) {
        return null;
    }

    @Override
    public Boolean visitTerminal(TerminalNode terminalNode) {
        return null;
    }

    @Override
    public Boolean visitErrorNode(ErrorNode errorNode) {
        return null;
    }
}
