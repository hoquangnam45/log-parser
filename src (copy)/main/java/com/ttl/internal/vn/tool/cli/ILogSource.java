package com.ttl.internal.vn.tool.cli;

import java.util.concurrent.Flow;

/**
 * Represent the source of the log object, which could be stacked with
 * the underlying object could be a file, a network stream,...
 */
public interface ILogSource {
    /**
     * @return fetch the data from the log stream which could be incompleted,
     * the receiver should be terminated log entry based on some indication from
     * the incompleted data, this method is irreversible, and each call will mutate
     * the underlying pointer
     */
    byte[] read();

    /**
     * @return Whether the underlying log source have available data waiting to be processed,
     * could be used to implement polling the source, or you could subscribe and receive latest
     * data as soon as possible without having to polling the source
     */
    boolean available();

    /**
     * Subscribes to the log source and receive latest data as soon as it is available
     */
    void subscribe(Flow.Subscriber<byte[]> subscriber);

    /**
     * Join multiple log sources together into single log source
     */
    void join(ILogSource anotherSource);

    /**
     * Reset the state of the log source
     */
    void reset();
}
