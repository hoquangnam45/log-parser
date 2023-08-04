package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
@Builder
public class LogEntry
{
	private final LogEntryLevel logLevel;
	private final String msgId;
	private final String sender;
	private final OffsetDateTime timestamp;
	private final String message;
}
