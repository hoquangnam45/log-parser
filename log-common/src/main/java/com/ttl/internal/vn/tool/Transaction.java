package com.ttl.internal.vn.tool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public abstract class Transaction<T> implements AutoCloseable
{
	public Transaction() {}

	private Transaction<?> enclosingTransaction;

	public abstract T runInTransaction(Transaction<?> outerTransaction) throws Exception;

	void setEnclosingTransaction(Transaction<?> enclosingTransaction) {
		this.enclosingTransaction = enclosingTransaction;
	}

	Transaction<?> getEnclosingTransaction() {
		return enclosingTransaction;
	}

	protected Connection getConnection() {
		return Optional.ofNullable(getEnclosingTransaction())
				.map(Transaction::getConnection)
				.orElse(null);
	}

	public void commit() throws SQLException
	{
		Transaction<?> transaction = getEnclosingTransaction();
		if (getEnclosingTransaction() != null) {
			transaction.commit();
		}
	}

	public void rollback() throws SQLException {
		Transaction<?> transaction = getEnclosingTransaction();
		if (getEnclosingTransaction() != null) {
			transaction.rollback();
		}
	}

	public void close() throws Exception {
		Transaction<?> transaction = getEnclosingTransaction();
		if (getEnclosingTransaction() != null) {
			transaction.close();
		}
	}
}
