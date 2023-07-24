package com.ttl.internal.vn.tool.log;

import java.util.List;
import java.util.stream.Stream;

public interface ILogSource extends Stream<ILogEntry> {
    String getID();
}
