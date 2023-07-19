package com.ttl.internal.vn.tool.cli;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogEntry {
    private String threadName;
    private LogEntryType logType;

    private String message;
}
