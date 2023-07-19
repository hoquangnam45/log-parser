package com.ttl.internal.vn.tool.cli;

public class LogPipe implements ILogPipe<> {
    private ILogSource source;

    @Override
    public void setSource(ILogSource logSource) {
        this.source = logSource;
    }

    @Override
    public void setTransformer(ILogTransformer logTransformer) {

    }

    @Override
    public void setFilter(ILogFilter filter) {

    }

    @Override
    public void setQuerier(ILogQuerier querier) {

    }
}
