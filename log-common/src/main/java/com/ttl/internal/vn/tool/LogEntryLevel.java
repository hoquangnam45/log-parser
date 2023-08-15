package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum LogEntryLevel
{
	ERROR(1, "ERR"),
	WARNING(2, "WRN"),
	INFO(4, "INF"),
	MESSAGE(8, "MSG"),
	NETWORK(16, "NET"),
	SQL(32, "SQL"),
	DEBUG(64, "DBG");

	private final int level;
	private final String shortName;

	public static LogEntryLevel parseValueShortName(String value) {
		return Stream.of(values()).filter(v -> v.shortName.equalsIgnoreCase(value)).findFirst().orElse(null);
	}
}
