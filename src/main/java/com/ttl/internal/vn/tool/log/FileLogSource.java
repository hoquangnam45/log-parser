package com.ttl.internal.vn.tool.log;

import com.ttl.internal.vn.tool.db.LogDB;
import com.ttl.internal.vn.tool.utils.DefaultSubscriber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Subscriber;

// List of file -> each file generate a individual line hash chain
// Input: log file -> generate a hash chain -> it check where the chain diverge from the db
// and prune the chain from the divergent section the recreate that section ->
// File -> hash chain -> check if
public class FileLogSource implements ILogSource
{
	private Subscriber<String> subscriber;
	private boolean       monitor    = false;
	private boolean pauseMonitor = false;
	private String incrementalHash;
	private final Queue<String> lineBuffer;
	private final ExecutorService executorService;
	private final File          logFile;
	private final MessageDigest hasher;
	private       LogDB         logDB;
	private String previousChainHash;

	public FileLogSource(File logFile) throws NoSuchAlgorithmException
	{
		this.logFile = logFile;
		this.executorService = Executors.newSingleThreadExecutor();
		this.lineBuffer = new LinkedList<>();
		this.hasher = MessageDigest.getInstance("SHA-256");
		this.logDB = new LogDB();
		this.registerSubscriber(new DefaultSubscriber<>() {
			@Override
			public void onNext(String line) {
				logDB.createLogEntry(previou);
			}
		});
	}

	public void registerSubscriber(Subscriber<String> subscriber) {
		this.subscriber = subscriber;
	}

	public synchronized void monitor() {
		if (!monitor)
		{
			monitor = true;
			pauseMonitor = false;
			executorService.submit(() -> {
				try
				{
					BufferedReader br = new BufferedReader(new FileReader(logFile));
					while (monitor) {
						while (pauseMonitor) {
							wait();
						}
						String newLine = br.readLine();
						if (newLine == null) {
							wait(500); // Currently no new line get added
							continue;
						}

						incrementalHash = new String(hasher.digest());
						hasher.update(newLine.getBytes());

						lineBuffer.add(newLine);
						if (subscriber != null) {
							while (lineBuffer.size() > 0) {
								subscriber.onNext(lineBuffer.remove());
							}
						}
					}
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
				finally
				{
					monitor = false;
					pauseMonitor = false;
					hasher.reset();
				}
			});
		}
	}

	public synchronized void stopMonitor() {
		if (monitor) {
			monitor = false;
		}
	}

	public synchronized void pauseMonitor() {
		if (!pauseMonitor) {
			pauseMonitor = true;
		}
	}

	public synchronized void resumeMonitor() {
		if (pauseMonitor) {
			pauseMonitor = false;
		}
	}

	public synchronized boolean isMonitor() {
		return monitor;
	}

	@Override
	public String getID()
	{
		return logFile.getAbsolutePath();
	}

	@Override
	public void refresh()
	{
		boolean resumeLater = false;
		if (isMonitor()) {
			resumeLater = true;
			stopMonitor();
		}

		String currentId = getID();
		if (resumeLater) {
			monitor();
		}
	}

	@Override
	public Iterator<ILogEntry> iterator()
	{
		return null;
	}


	public void append(String str) {

	}
}
