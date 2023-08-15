package com.ttl.internal.vn.tool;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// A log block is a partial log session, 1 log file can contain multiple log block, a log block never span multiple files
@AllArgsConstructor
@Getter
@Builder
public abstract class LogBlock {
    private final LogBlockType type;
    private final FileChunk chunk;
    private final List<LogEntry> entries;
    private final List<Line> environments;

    public void placeAfter(Range range) {
        chunk.placeAfter(range);
    }

    public void addEnvironmentLine(Line line) {
        environments.add(line);
        chunk.getRange().expandRange(line.toRange());
    }

    public void addLogEntry(LogEntry entry) {
        entries.add(entry);
        chunk.getRange().expandRange(entry.getChunk().getRange());
    }

    public void appendMessageToLogEntry(Line line) {
        // NOTE: This method should always be called after log entry so no need to check for size
        entries.get(entries.size() - 1).appendMessageToLogEntry(line);
        chunk.getRange().expandRange(line.toRange());
    }

    public List<LogBlockChange> getChanges(FileDiff diff) {

    }
}
