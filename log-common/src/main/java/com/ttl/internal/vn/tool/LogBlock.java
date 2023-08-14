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
public class LogBlock implements Cloneable {
    private final LogBlockType type;
    private final List<LogEntry> entries;
    private final Range range;

    @Override
    public LogBlock clone() {
        return LogBlock.builder()
                .type(type)
                .range(range)
                .entries(Optional.ofNullable(entries)
                        .map(entries -> entries.stream().map(LogEntry::clone).collect(Collectors.toList()))
                        .orElse(null))
                .build();
    }

    public LogBlock after(LogBlock beforeBlock) {
        Range beforeBlockRange = beforeBlock.getRange();
        int newBegin = beforeBlockRange.getEnd() + 1;
        Range newRange = Range.builder()
                .begin(newBegin)
                .end(newBegin + range.getEnd() - range.getBegin())
                .build();
        List<LogEntry> newEntries = Optional.ofNullable(entries)
                .map(entries -> {
                    LogEntry previousNewEntry = null;
                    List<LogEntry> innerNewEntries = new ArrayList<>();
                    for (int i = 0; i < entries.size(); i++) {
                        LogEntry currentEntry = entries.get(i);
                        if (previousNewEntry == null) {
                            previousNewEntry = currentEntry.moveByDistance(newBegin - range.getBegin());
                            innerNewEntries.add(previousNewEntry);
                        } else {
                            previousNewEntry = currentEntry.after(previousNewEntry);
                            innerNewEntries.add(previousNewEntry);
                        }
                    }
                    return innerNewEntries;
                })
                .orElse(null);
        return LogBlock.builder()
                .type(type)
                .range(newRange)
                .entries(newEntries)
                .build();
    }
}
