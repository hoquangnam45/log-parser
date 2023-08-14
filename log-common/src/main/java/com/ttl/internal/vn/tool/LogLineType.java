package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum LogLineType {
    MAYBE_HEADER(true),
    MESSAGE(false),
    NEW_ENTRY(false);

    private final boolean shouldBuffer;

}
