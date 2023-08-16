package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class LogBlockDeltaChange {
    private LogBlockChangeType type;
    private LogBlock block;
    private LogEntryDeltaChange entryChange;
    private List<Line> environmentChanges;
}
