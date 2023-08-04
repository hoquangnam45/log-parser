package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum LogEntryLevel
{
	FATAL(1),
	ERROR(1),
	WARN(2),
	INFO(4),
	MESG(8),
	NET(16),
	SQL(32),
	DEBUG(64);

	private final int level;

	public static LogEntryLevel parseValue(String value) {
		return Stream.of(values()).filter(v -> v.name().equalsIgnoreCase(value)).findFirst().orElse(null);
	}

	public static LogEntryLevel parseValue(Integer value) {
		return Stream.of(values()).filter(v -> Integer.valueOf(v.getLevel()).equals(value)).findFirst().orElse(null);
	}
}
