package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@Getter
public class LogSession {
    private UUID uuid;

    private LogBlock startupBlock;
    // All rolling file log blocks
    private List<LogBlock> blocks;

    public static LogSession startNewSession() {
        return LogSession.builder()
                .blocks(new ArrayList<>())
                .uuid(UUID.randomUUID())
                .build();
    }

    public Map<String, String> getJavaEnvironments() {
        return environments.computeIfAbsent("Java Runtime Environment", k -> new ArrayList<>()).stream()
                .map(it -> it.split("="))
                .map(it -> Pair.of(it[0].trim(), it[1].trim()))
                .collect(Collectors.toMap(it -> it.getKey(), it -> it.getValue()));
    }

    public List<String> getTcpIpConfigurations() {
        return environments.computeIfAbsent("TCP/IP Configuration", k -> new ArrayList<>());
    }

    // Getting the timestamp of the first log block
    public OffsetDateTime getStartTime() {
        if (blocks.isEmpty()) {
            throw new IllegalStateException("Not possible to enter here");
        }
        return blocks.get(0).getTimestamp();
    }

    // Getting the timestamp of the last log block
    public OffsetDateTime getEndtime() {
        if (blocks.isEmpty()) {
            throw new IllegalStateException("Not possible to enter here");
        }
        return blocks.get(blocks.size() -1).getTimestamp();
    }

    // Getting the duration between the first and the last log block in the log session
    public Duration getDuration() {
        return Duration.between(getStartTime(), getEndtime());
    }

    public void appendLogBlock(LogBlock logBlock) {

    }
}