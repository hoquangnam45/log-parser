package com.ttl.internal.vn.tool.cli;

/**
 * Represent a complete log object with the ability to perform query operation on it,
 * source -> transformer -> filter -> querier
 * @param <T>: Represent the data structure that will contain the query request
 * @param <K>: Represent the data structure of an entry in log stream
 */
public interface ILogPipe<T, K> {
    void setSource(ILogSource logSource);
    void setTransformer(ILogTransformer<K> logTransformer);
    void setFilter(ILogFilter<K> filter);
    void setQuerier(ILogQuerier<T> querier);
}
