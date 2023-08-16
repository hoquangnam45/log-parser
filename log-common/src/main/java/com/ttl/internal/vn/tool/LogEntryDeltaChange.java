package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class LogEntryDeltaChange {
    private LogEntryChangeType type;
    private LogEntry logEntry;
    private List<Line> changedLines;
}
