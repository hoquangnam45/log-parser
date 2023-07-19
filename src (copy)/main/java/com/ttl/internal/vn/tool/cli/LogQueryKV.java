package com.ttl.internal.vn.tool.cli;

import lombok.Getter;

@Getter
public class LogQueryKV {
    private String key;
    private String value;
    private String queryType;

    public LogQueryKV(String query) {
        query = query.trim();

    }
}
