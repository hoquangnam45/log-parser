package com.ttl.internal.vn.tool.cli;

import java.util.Map;

/**
 * Interact with the underlying log object and perform the necessary query
 * to get log insight
 */
public interface ILogQuerier<T> {
    Map<String, Object> query(T query);
}
