package com.ttl.internal.vn.tool.log;

import java.util.concurrent.Flow.Subscriber;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ILogSource
{
    String getID();
    void refresh();
}
