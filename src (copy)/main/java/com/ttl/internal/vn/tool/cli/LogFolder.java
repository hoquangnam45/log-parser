package com.ttl.internal.vn.tool.cli;

import com.ttl.internal.vn.tool.utils.Extensions;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogFolder implements ILogSource {
    private final List<byte[]> bufferQueue;
    private final List<Flow.Subscriber<byte[]>> subscribers;

    private List<ILogSource> sources;
    private List<Flow.Subscription> subscriptions;
    private final String path;
    private int bufferPointer;

    public LogFolder(String path) {
        this.bufferQueue = new LinkedList<>();
        this.subscribers = new ArrayList<>();
        this.path = path;
        initSources();
    }

    void initSources() {
        this.sources = new ArrayList<>();
        this.subscriptions = new ArrayList<>();
        Optional.ofNullable(this.path)
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(File::new)
                .map(File::listFiles)
                .map(Stream::of)
                .ifPresent(stream -> stream.filter(Extensions::isLogFile).map(LogFile::new).forEach(this::join));
    }

    @Override
    public byte[] read() {
        bufferPointer++;
        return bufferQueue.;
    }

    @Override
    public boolean available() {
        return bufferQueue.size() > 0;
    }

    @Override
    public void subscribe(Flow.Subscriber<byte[]> subscriber) {
        int length = subscribers.size();
        subscribers.add(subscriber);
        subscriber.onSubscribe(new DefaultSubscription() {
            @Override
            public void cancel() {
                subscribers.remove(length);
            }
        });
    }

    void initSource(ILogSource source) {
        source.subscribe(new DefaultSubscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscriptions.add(subscription);
            }

            @Override
            public void onNext(byte[] data) {
                bufferQueue.add(data);
                subscribers.forEach(s -> s.onNext(data));
            }
        });
    }
    @Override
    public void join(ILogSource anotherSource) {
        sources.add(anotherSource);
        initSource(anotherSource);
    }

    @Override
    public void reset() {
        subscriptions.forEach(Flow.Subscription::cancel);
        bufferQueue.clear();
        bufferPointer = 0;
        initSources();
    }
}
