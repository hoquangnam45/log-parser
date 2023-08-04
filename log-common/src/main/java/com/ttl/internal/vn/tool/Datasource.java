package com.ttl.internal.vn.tool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Datasource {
	private static Datasource INSTANCE;

	public static synchronized void init(HikariConfig config) {
		if (INSTANCE != null)
		{
			INSTANCE = new Datasource(config);
		}
	}

	public static Datasource getInstance() {
		return INSTANCE;
	}

	private final HikariDataSource ds;

	public Datasource(HikariConfig config) {
		ds = new HikariDataSource(config);
	}

	public Connection getConnection() throws SQLException
	{
		return ds.getConnection();
	}
}
