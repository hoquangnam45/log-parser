package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// NOTE: A log entry started from a log entry header and end when another log entry header is found
@Getter
@AllArgsConstructor
@Builder
public class LogEntry implements Cloneable
{
	private final LogEntryLevel logLevel;
	private final String msgId;
	private final String sender;
	private final OffsetDateTime timestamp;
	private final List<String> messages; // each message corresponds to a line
	private final Range range;

	private Stream<String> toStream() {
		return messages.stream();
	}

	@Override
	public LogEntry clone() {
		return LogEntry.builder()
				.logLevel(logLevel)
				.msgId(msgId)
				.sender(sender)
				.timestamp(timestamp)
				.messages(messages.stream().collect(Collectors.toList()))
				.range(range)
				.build();
	}

	public LogEntry after(LogEntry beforeEntry) {
		return LogEntry.builder()
				.logLevel(logLevel)
				.msgId(msgId)
				.sender(sender)
				.timestamp(timestamp)
				.messages(messages.stream().collect(Collectors.toList()))
				.range(range.after(beforeEntry.getRange()))
				.build();
	}

	public LogEntry moveByDistance(int distance) {
		return LogEntry.builder()
				.logLevel(logLevel)
				.msgId(msgId)
				.sender(sender)
				.timestamp(timestamp)
				.messages(messages.stream().collect(Collectors.toList()))
				.range(range.moveByDistance(distance))
				.build();
	}
}
