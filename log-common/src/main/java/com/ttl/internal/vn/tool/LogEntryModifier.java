package com.ttl.internal.vn.tool;

import java.util.function.Function;

public class LogEntryModifier
{
	public LogEntry modifyMessage(LogEntry logEntry, Function<String, String> messageModifier) {
		return LogEntry.builder()
				.logLevel(logEntry.getLogLevel())
				.msgId(logEntry.getMsgId())
				.sender(logEntry.getSender())
				.timestamp(logEntry.getTimestamp())
				.message(messageModifier.apply(logEntry.getMessage()))
				.build();
	}
}
