package com.ttl.internal.vn.tool.cli;

import java.util.concurrent.Flow;


public abstract class DefaultSubscription implements Flow.Subscription {
    @Override
    public void request(long n) {
    }

    @Override
    public void cancel() {
    }
}
