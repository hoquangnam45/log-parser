package com.ttl.internal.vn.tool.utils;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class DefaultSubscriber<T> implements Subscriber<T>
{
	@Override
	public void onSubscribe(Subscription subscription)
	{
	}

	@Override
	public void onNext(T item)
	{
	}

	@Override
	public void onError(Throwable throwable)
	{
	}

	@Override
	public void onComplete()
	{
	}
}
