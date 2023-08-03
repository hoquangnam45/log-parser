package com.ttl.internal.vn.tool;

import com.ttl.internal.vn.tool.grammar.QueryRulesLexer;
import com.ttl.internal.vn.tool.grammar.QueryRulesParser;
import com.ttl.internal.vn.tool.log.FileLogSource;
import com.ttl.internal.vn.tool.log.ILogEntry;
import com.ttl.internal.vn.tool.query.QueryExprVisitor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class App 
{
    // NOTE: This is for testing purposes
    public static void main( String[] args )
    {
        FileLogSource logSource = new FileLogSource("/tmp/log");
        String query = "in(\"abc.log\") filter @entry.threadName == \"thread#1\"";
        QueryExprVisitor visitor = new QueryExprVisitor();
        QueryRulesLexer lexer = new QueryRulesLexer(CharStreams.fromString(query));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        QueryRulesParser parser = new QueryRulesParser(tokens);
        visitor.visitFull_query_expr(parser.full_query_expr()).toStream().forEach(entry -> {
            // TODO: Construct view either in terminal or UI
        });
    }
}
