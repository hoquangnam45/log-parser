package com.ttl.internal.vn.tool;

public class LogIngestor
{
	private final String dbUsername;
	private final String dbPassword;
	private final String dbUrl;

	private Datasource datasource

	public LogIngestor(String dbUsername, String dbPassword, String dbUrl) {
		this.dbUsername = dbUsername;
		this.dbPassword = dbPassword;
		this.dbUrl = dbUrl;
	}

	public void start() {
		// Open a connection to db

	}
}