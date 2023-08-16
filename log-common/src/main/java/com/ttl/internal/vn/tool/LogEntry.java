package com.ttl.internal.vn.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

// NOTE: A log entry started from a log entry header and end when another log entry header is found
@Getter
@AllArgsConstructor
@Builder
public class LogEntry
{
	private final LogEntryLevel logLevel;
	// Not know what this mean yet, but this exists was mentioned inside the source
	private final String msgId;
	private final String sender;
	private final OffsetDateTime timestamp;
	// each message corresponds to a line, this is as raw as possible as it include the unparsed metadata
	private final List<Line> lines;
	private final Line firstLineMessage;
	private final FileChunk chunk;

	public void placeAfter(Range range) {
		chunk.placeAfter(range);
	}

	// This method get by line and does not include unparsed metadata in its return value
	public String getMessage(int line) {
		if (line == chunk.getRange().getBegin()) {
			return firstLineMessage.getLine();
		}
		return lines.get(line - chunk.getRange().getBegin()).getLine();
	}

    public void appendMessageToLogEntry(Line line) {
		lines.add(line);
		chunk.getRange().expandRange(line.toRange());
    }

	public List<LogEntryDeltaChange> getChanges(FileDiff diff) {

	}
}
