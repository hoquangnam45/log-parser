package com.ttl.internal.vn.tool.cli;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

@Builder
@Getter
public abstract class LogQuery {
    private List<LogQuery> logQueries;
    private Class<?> acceptedType;

    // NOTE: I could use sql for this but sql is quite overkilled for this
    public static LogQuery of(String queryStr) {
        // Trim query string
        queryStr = queryStr.trim();

        LogQueryNode rootNode = new LogQueryNode(Pair.of(0, queryStr.length()));

        // Get bracket index
        List<String> tokens = new ArrayList<>();
        Stack<Integer> openBracketIndices = new Stack<>();
        List<Pair<Integer, Integer>> brackets = new ArrayList<>();
        for (int i = 0; i < queryStr.length(); i++) {
            char c = queryStr.charAt(i);
            if (c == '(') {
                openBracketIndices.add(i);
            } else if (c == ')') {
                // Pop closest open bracket from the stack
                int openBracketIndex = openBracketIndices.pop();
                brackets.add(Pair.of(openBracketIndex, i));
            }
        }
        // Ill-formed query where bracket is not closed properly
        if (openBracketIndices.size() > 0) {
            throw new IllegalStateException("Brackets is not closed properly");
        }

        // Build the log query tree
        for (int i = brackets.size() - 1; i >= 0; i--) {
            rootNode.addChild(brackets.get(i));
        }

        // Traverse down the log query tree and turn each expression at each level to new node
        List<Pair<Integer, Integer>> atomicRanges = new ArrayList<>();
        List<Pair<Integer, Integer>> ranges = new ArrayList<>();
        if (rootNode.getChildNodes().size() == 0) {
            ranges.add(rootNode.getRange());
        }
        rootNode.getChildNodes().add(Pair.of())
        for (int i = 0; i < rootNode.getChildNodes().size(); i++) {
            root
        }

        List
        // Tokenized based on AND, OR expressions
        for (Pair<Integer, Integer> bracket: brackets) {

        }
        // Order evaluation order based on AND, OR expressions

        // Tokenize based on operators (>=, <=, >, <, ==, !=, regexp(...))

        // Tokenize based on query keyword and value

        // Deduce the type to perform operator based on keywords

        List<LogQueryKV> logQueryKvs = new ArrayList<>();
        // Map all the above info into a single predicate that can be used to apply on a type
        logQueryKvs.stream().map(it -> {
            Class<?> acceptedType;
            switch (it.getKey()) {
                case "threadName": {
                    acceptedType = String.class;
                    break;
                }
                case "timestamp": {
                    acceptedType = LocalDateTime.class;
                    break;
                }
                case "logLevel": {
                    acceptedType = LogLevel.class;
                    break;
                }
                case "logType": {
                    acceptedType = LogType.class;
                    break;
                }
            }
        })
        String tokens =
        return LogQuery
    }

    public boolean match(Object rhs) {
        if (acceptedType.isAssignableFrom(rhs.getClass())) {
            return false;
        }
        logQueries.stream().map()
    };

    // Atomic range mean there no bracket in the range and no AND, OR
    private LogQuery ofAtomic(String queryString, Pair<Integer, Integer> atomicRange) {
        List<String> orTokens = Arrays.asList(queryString.split("||"));
        String subQueryString
        orTokens.stream().map(String::trim).forEach(token -> {
            List<String> andTokens = Arrays.asList("&&");
            andTokens.stream().forEach();
        });
        f
    }

    private
}
