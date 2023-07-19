package com.ttl.internal.vn.tool.cli;

/**
 * Represent the log filter, which could be used to provide some filter
 * some useful information from the log source as soon as it is available
 */
public interface ILogFilter<T> {
    boolean filter(T logEntry);
}
