package com.ttl.internal.vn.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class LogFileUpdater {
    public LogFile updateLogFileWithDiff(LogFile logFile, List<FileDiff> diffs) {
        // diff -> affected blocks
        Map<FileDiff, List<Integer>> blocksAffectedByDiffs = new HashMap<>();

        int diffIndex = 0;
        for (int i = 0; i < logFile.getBlocks().size(); i++) {
            if (diffIndex < diffs.size()) {
                FileDiff diff = diffs.get(diffIndex);
                LogBlock logBlock = logFile.getBlocks().get(i);
                if (isAffected(logBlock.getRange(), diff.getRange())) {
                    blocksAffectedByDiffs.computeIfAbsent(diff, unused -> new ArrayList<>()).add(i);
                } else {
                    diffIndex++;
                }
            } else {
                break;
            }
        }

        // diff -> affected blocks index -> affected log entries index
        Map<FileDiff, Map<Integer, List<Integer>>> entriesAffectedByDiffs = new HashMap<>();
        for (Entry<FileDiff, List<Integer>> entry : blocksAffectedByDiffs.entrySet()) {
            FileDiff diff = entry.getKey();
            for (Integer affectedBlockIndex : entry.getValue()) {
                LogBlock affectedBlock = logFile.getBlocks().get(affectedBlockIndex);
                for (int i = 0; i < affectedBlock.getEntries().size(); i++) {
                    LogEntry logEntry = affectedBlock.getEntries().get(i);
                    if (isAffected(logEntry.getRange(), diff.getRange())) {
                        entriesAffectedByDiffs
                                .computeIfAbsent(diff, unused -> new HashMap<>())
                                .computeIfAbsent(affectedBlockIndex, unused -> new ArrayList<>())
                                .add(i);
                    }
                }
            }
        }

        entriesAffectedByDiffs.entrySet().
        // Affected blocks index
        List<LogBlock> newBlocks = new ArrayList<>();

        LogFile newLogFile = LogFile.builder()
                .file(logFile.getFile())
                .blocks(new ArrayList<>())
                .build();

        for (int i = 0; i < logFile.getBlocks().size(); i++) {
            LogBlock logBlock = logFile.getBlocks().get(i);

            if (!affectedBlocks.contains(i)) {
                logFile.appendNewBlock(logBlock);
            } else {
                List<LogBlock> updatedBlocks = recreatedLogBlocks(affectedBlocks, diff);
            }
        }
    }

    public List<LogBlock> recreatedLogBlocks(Map<Integer, List<Integer>> affectedBlocks, FileDiff diff, LogFile logFile) {

    }


    private boolean isAffected(Range currentRange, Range diffRange) {
        if (currentRange.getEnd() >= diffRange.getBegin() || currentRange.getBegin() <= diffRange.getEnd()) {
            return true;
        }
        return false;
    }
}
