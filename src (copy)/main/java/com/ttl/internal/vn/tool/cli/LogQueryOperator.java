package com.ttl.internal.vn.tool.cli;

public enum LogQueryOperator {
    EMPTY,
    LARGER,
    EQUALS,
    SMALLER,
    NOT,
    AND,
    OR,
    REGEX;

    private LogQueryOperator valueOf(String operator) {

    }
}
