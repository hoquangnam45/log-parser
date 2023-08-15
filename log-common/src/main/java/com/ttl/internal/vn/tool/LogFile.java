package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static java.util.function.Predicate.not;

@Getter
@AllArgsConstructor
@Builder
public class LogFile {
    private final List<LogBlock> blocks;
    private final File file;

    public LogBlock getLastBlock() {
        return Optional.ofNullable(blocks).filter(not(List::isEmpty)).map(it -> it.get(it.size() - 1)).orElse(null);
    }

    public void appendNewBlock(LogBlock block) {
        Optional.of(getLastBlock()).ifPresent(lastBlock -> block.placeAfter(lastBlock.getChunk().getRange()));
        blocks.add(block);
    }
}
