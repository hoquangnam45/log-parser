package com.ttl.internal.vn.tool.cli;

public interface ILogStream {
    /**
     *  @return the hash of log stream, will be used to determine whether to
     *  mutate this object if encounter new data
     */
    String hash();
    /**
     *  @return the type of the log stream
     */
    LogType type();

    /**
     *
     */
    void join(ILogSource anotherSource);
}
