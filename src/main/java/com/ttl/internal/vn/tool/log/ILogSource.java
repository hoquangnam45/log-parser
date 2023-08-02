package com.ttl.internal.vn.tool.log;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ILogSource extends Iterable<ILogEntry>, Cloneable
{
    String getID();
    void refresh();

    default Stream<ILogEntry> toStream() {
        return StreamSupport.stream(spliterator(), false);
    }
    ILogSource add(ILogSource anotherSource);
}
