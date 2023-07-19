package com.ttl.internal.vn.tool.cli;

import java.util.concurrent.Flow;

public abstract class DefaultSubscriber<T> implements Flow.Subscriber<T> {
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
    }

    @Override
    public void onNext(T item) {
    }

    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onComplete() {
    }
}
