package com.ttl.internal.vn.tool.cli;

public interface ILogTransformer<T> {
    T transform (byte[] logEntry);
}
