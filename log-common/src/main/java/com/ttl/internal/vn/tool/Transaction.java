package com.ttl.internal.vn.tool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public abstract class Transaction<T> implements AutoCloseable
{
	public Transaction() {}

	private Transaction<?> enclosingTransaction;

	public abstract T runInTransaction() throws Exception;

	protected void setEnclosingTransaction(Transaction<?> enclosingTransaction) {
		if (enclosingTransaction == this.enclosingTransaction) {
			return;
		}
		if (this.enclosingTransaction != null) {
			throw new IllegalStateException("This transaction is already run in another enclosing transaction");
		}
		this.enclosingTransaction = enclosingTransaction;
	}

	public Transaction<?> getEnclosingTransaction() {
		return enclosingTransaction;
	}

	protected Connection getConnection() {
		if (getEnclosingTransaction() != null) {
			return getEnclosingTransaction().getConnection();
		}
		throw new RuntimeException("Should not happen since enclosing transaction should have overriden this method");
	}

	public void commit() throws SQLException
	{
		if (getEnclosingTransaction() == null) {
			if (getConnection().isValid(0)) {
				getConnection().commit();
			}
		}
	}

	public void rollback() throws SQLException {
		if (getConnection().isValid(0)) {
			getConnection().rollback();
		}
	}

	public void close() throws Exception {}
}
