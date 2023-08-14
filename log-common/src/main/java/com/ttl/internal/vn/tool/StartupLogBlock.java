package com.ttl.internal.vn.tool;

import java.time.OffsetDateTime;
import java.util.List;

public class StartupLogBlock extends LogBlock {

    public StartupLogBlock(LogEntryLevel logLevel, String msgId, String sender, OffsetDateTime timestamp, String message, List<LogEntry> entries, FileChunk chunk) {
        super(LogBlockType.STARTUP_HEADER, entries, chunk);
    }
}
