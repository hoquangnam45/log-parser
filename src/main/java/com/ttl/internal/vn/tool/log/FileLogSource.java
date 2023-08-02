package com.ttl.internal.vn.tool.log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FileLogSource implements ILogSource
{
	// NOTE: lineNumber -> listIndex
	private Map<Integer, Integer> lineNumberMap = new HashMap<>();
	private List<ILogEntry> logEntries = new ArrayList<>();

	private final File logFile;

	public FileLogSource(File logFile) {
		this.logFile = logFile;
	}

	@Override
	public String getID()
	{
		return logFile.getAbsolutePath();
	}

	@Override
	public void refresh()
	{

	}

	@Override
	public Iterator<ILogEntry> iterator()
	{
		return null;
	}


	public void append(String str) {

	}
}
