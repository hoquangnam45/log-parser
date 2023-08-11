package com.ttl.internal.vn.tool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Datasource {
	private final HikariDataSource ds;

	public Datasource(HikariConfig config) {
		ds = new HikariDataSource(config);
	}

	public Connection getConnection() throws SQLException
	{
		return ds.getConnection();
	}
}
