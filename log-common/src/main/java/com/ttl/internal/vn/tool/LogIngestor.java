package com.ttl.internal.vn.tool;


import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Date;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Multiple log entries -> log blocks -> log sessions
@Getter
@Builder
public class LogIngestor
{
	// NOTE: Pattern example: [INF TaskSchedular,04-03 17:56:57.172]: abc
	private static final Pattern LOG_NEW_ENTRY_LINE_PATTERN = Pattern.compile("\\[([A-Z]{3,}) \\s*(\\S+)\\s*,\\s*(\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}(.\\d+)*)\\]\\s*:(.*)");
	private final File logFile;
	private final Queue<Line> headerBuffer;

	private LogBlock currentBlock;
	private boolean logEntryEncounter;
	private int countHeaderEncounter;

	public LogIngestor(File logFile) {
		this.logFile = logFile;
		this.headerBuffer = new ArrayDeque<>();
	}

	public Observable<LogBlock> processLine(Line line) {
		return getLineType(line).flatMap(pair -> {
			LogLineType type = pair.getRight();
			Line innerLine = pair.getLeft();
			switch (type) {
				case STARTUP_HEADER:
					// Reset the log entry so next encounter of message type line without log entry will send line -> environment,
					// otherwise send line -> current log entry
					logEntryEncounter = false;

					if (countHeaderEncounter == 0) {
						// New header encounter -> indicate that new block is about to start
						currentBlock = new StartupLogBlock(FileChunk.builder()
								.file(logFile)
								.range(innerLine.toRange())
								.build());
					} else {
						// Part of the header, increase the range
						currentBlock.getChunk().getRange().expandRange(innerLine.toRange());
					}
					countHeaderEncounter++;
					break;
				case ROLLING_HEADER:
					// Reset the log entry so next encounter of message type line without log entry will send line -> environment,
					// otherwise send line -> current log entry
					logEntryEncounter = false;

					if (countHeaderEncounter == 0) {
						// New header encounter -> indicate that new block is about to start
						currentBlock = new RollingLogBlock(FileChunk.builder()
								.file(logFile)
								.range(innerLine.toRange())
								.build());
					} else {
						// Part of the header, increase the range
						currentBlock.getChunk().getRange().expandRange(innerLine.toRange());
					}
					countHeaderEncounter++;
					break;
				case MESSAGE:
					// Reset the count so next encounter of header line will create a new block
					countHeaderEncounter = 0;

					// Increase the log block range
					currentBlock.getChunk().getRange().expandRange(innerLine.toRange());

					if (!logEntryEncounter) {
						// line is part of the environments
						currentBlock.addEnvironmentLine(innerLine);
					} else {
						currentBlock.appendMessageToLogEntry(innerLine);
					}

					break;
				case LOG_ENTRY:
					// Reset the count so next encounter of header line will create a new block
					countHeaderEncounter = 0;

					// Increase the log block range
					currentBlock.getChunk().getRange().expandRange(innerLine.toRange());

					Matcher m = LOG_NEW_ENTRY_LINE_PATTERN.matcher(innerLine.getLine());

					// This timestamp does not include the year so 1 workaround is to add year
					// to this string based on file created date
					// Ex: 12-30 00:00:03.304
					String timestamp = m.group(3);
					BasicFileAttributes attrs = Files.readAttributes(logFile.toPath(), BasicFileAttributes.class);
					timestamp = OffsetDateTime.from(attrs.creationTime().toInstant()).getYear() + "-" + timestamp;

					// Allow timestamp to have millisecond 0 to 3 millisecond
					DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm:ss") // .parseLenient()
							.appendFraction(ChronoField.NANO_OF_SECOND, 0, 3, true).toFormatter();

					LogEntry newEntry = LogEntry.builder()
							.lines(new ArrayList<>() {{ add(innerLine); }})
							.msgId(null)
							.sender(m.group(2))
							.logLevel(LogEntryLevel.parseValueShortName(m.group(1)))
							.chunk(FileChunk.builder().range(innerLine.toRange()).file(logFile).build())
							.timestamp(OffsetDateTime.parse(timestamp, formatter))
							.firstLineMessage(Line.builder().build())
							.build();

					if (logEntryEncounter == false) {
						return Observable.just(currentBlock);
						logEntryEncounter = true;
					}
					currentBlock.addLogEntry(newEntry);
					break;
				default:
					throw new IllegalStateException("Not possible to enter here");
			}

			return Observable.empty();
		});
	}

	public void storeBlockToDB(LogBlock logBlock) {
		switch (logBlock.getType()) {
			case STARTUP:

			case ROLL_FILE:
				// Find out which startup block this roll file belongs to
				// As roll file is
		}
	}

	public Observable<Line> loadLogFile(boolean watch, Duration interval) throws IOException {
		// Copy log file to a temporary directory to prevent modification while loading
		File tmpLogFile = File.createTempFile("temp", "");
		FileUtils.copyFile(logFile, tmpLogFile);
		try (Reader reader = new FileReader(tmpLogFile);
			 BufferedReader br = new BufferedReader(reader)
		) {
			if (!watch) {
				return Observable.create(s -> {
					String line;
					int lineNumber = 0;
					while ((line = br.readLine()) != null) {
						lineNumber++;
						s.onNext(Line.builder().line(line).lineNumber(lineNumber).build());
					}
					s.onComplete();
				});
			} else {
				return Observable.create(s -> {
					String line;
					int lineNumber = 0;
					while (!s.isDisposed()) {
						if ((line = br.readLine()) != null) {
							lineNumber++;
							s.onNext(Line.builder().line(line).lineNumber(lineNumber).build());
						}
						wait(interval.toMillis());
					}
					s.onComplete();
				});
			}
		}
	}

	// This method does not immediately emit a line type when receive the line, it may buffer for some number of line
	// then emit it later after it confident what type that line is
	private Observable<Pair<Line, LogLineType>> getLineType(Line line) {
		return Observable.<Observable<Pair<Line, LogLineType>>>create(e -> {
			Observable<Pair<Line, LogLineType>> bufferableLineTypes = Observable.empty();

			// Process buffer first so that events is emitted in order
			if (line.getLine().startsWith("*")) {
				// Buffer it since it maybe a header, not emit the line type immediately
				headerBuffer.add(line);
			} else {
				// Interact with header buffer queue to determine whether those line that has been buffered is just a
				// message or maybe a header
				bufferableLineTypes.mergeWith(Observable.create(innerE -> {
					// The header buffer has less than 2 items so this certainly not a header just a normal message
					if (headerBuffer.size() < 2) {
						while (headerBuffer.peek() != null) {
							innerE.onNext(Pair.of(headerBuffer.poll(), LogLineType.MESSAGE));
						}
					} else {
						// Use moving windows with size of 3 to determine whether this is just a message or a header
						Queue<Line> movingWindow = new LinkedList<>();
						while (headerBuffer.peek() != null) {
							movingWindow.add(headerBuffer.poll());

							if (movingWindow.size() < 3) {
								// Fill the moving window until it meets minimum of 3 line
								continue;
							}

							// more than 3 lines but the moving windows still not found the header, so all the first line is certainly not a header
							while (movingWindow.size() > 3) {
								innerE.onNext(Pair.of(movingWindow.poll(), LogLineType.MESSAGE));
							}

							// Check current 3 lines in moving windows to see if really a  header
							List<String> lines = movingWindow.stream().map(Line::getLine).collect(Collectors.toList());

							// A header has all 3 line at the same length, contains * only for the 1st and 3rd line,
							// and a header type of either system start up or new file for 2nd line
							String headerType = lines.get(1).replaceAll("\\*", "").trim();
							boolean isHeader =
									lines.get(0).length() == lines.get(1).length() &&
									lines.get(1).length() == lines.get(2).length() &&
									lines.get(0).length() > 0 &&
									lines.get(0).replaceAll("\\*", "").length() == 0 && // Check if 1st line is all wildcard
									lines.get(2).replaceAll("\\*", "").length() == 0 && // Check if 3rd line is all wildcard
									("System Startup".equals(headerType) || "New File".equals(headerType));

							if (isHeader) {
								LogLineType type;
								switch (headerType) {
									case "System Startup":
										type = LogLineType.STARTUP_HEADER;
										break;
									case "New File":
										type = LogLineType.ROLLING_HEADER;
										break;
									default:
										throw new IllegalStateException("Not possible to enter here");
								}

								// Empty the moving windows that contains the header
								while (movingWindow.size() > 0) {
									innerE.onNext(Pair.of(movingWindow.poll(), type));
								}
							}
						}

						// After moving windows processing but some line still remain in the moving windows indicate those
						// remaining line is certainly not a header, empty it out as message type
						while (!movingWindow.isEmpty()) {
							innerE.onNext(Pair.of(movingWindow.poll(), LogLineType.MESSAGE));
						}
					}
				}));

				// Check if it's new log entry pattern
				Matcher m = LOG_NEW_ENTRY_LINE_PATTERN.matcher(line.getLine());
				if (m.find()) {
					bufferableLineTypes.mergeWith(Observable.just(Pair.of(line, LogLineType.LOG_ENTRY)));
				} else {
					// Just a log message
					bufferableLineTypes.mergeWith(Observable.just(Pair.of(line, LogLineType.MESSAGE)));
				}

				// Emit the type of line in the buffer after processing then any other type that can be determined
				// immediately without resorting to buffer
				e.onNext(bufferableLineTypes);
				e.onComplete();
			}
		}).flatMap(it -> it);
	}
}