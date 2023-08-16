package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
@AllArgsConstructor
public class LogBlockRepo {
    private final Connection connection;

    public void storeStartupLogBlock(StartupLogBlock logBlock) {

    }

    public void storeRollingLogBlock(RollingLogBlock logBlock) {
    }

    public void appendToLogEntry(LogEntry logEntry) {
    }

    public void storeLogEntry(LogBlock logBlock, LogEntry logEntry) {
    }

    public void storeStartupLogBlockEnvironments(List<Line> environmentChanges) {
        Stream<Line> lineStream = environmentChanges.stream().filter(line -> StringUtils.isNotBlank(line.getLine()));
        Iterator<Line> iterator = lineStream.iterator();
        for (Line line = iterator.next(); iterator.hasNext(); iterator.next()) {
            Pair<String, String> kv = line.
        }
    }
}
