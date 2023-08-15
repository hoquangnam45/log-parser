package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum LogLineType {
    STARTUP_HEADER,
    ROLLING_HEADER,
    MESSAGE,
    LOG_ENTRY,
}
