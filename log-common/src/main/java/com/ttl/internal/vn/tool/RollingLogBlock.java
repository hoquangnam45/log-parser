package com.ttl.internal.vn.tool;

import java.time.OffsetDateTime;
import java.util.List;

public class RollingLogBlock extends LogBlock {
    public RollingLogBlock(List<LogEntry> entries, FileChunk chunk) {
        super(LogBlockType.ROLLFILE_HEADER, entries, chunk);
    }
}
