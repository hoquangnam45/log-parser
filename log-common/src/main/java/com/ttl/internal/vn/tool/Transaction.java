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
		return Optional.ofNullable(getEnclosingTransaction())
				.map(Transaction::getConnection)
				.orElse(null);
	}

	public void commit() throws SQLException
	{
		Transaction<?> transaction = getEnclosingTransaction();
		if (getEnclosingTransaction() != null) {
			transaction.commit();
		} else {
			getEnclosingTransaction().commit();
		}
	}

	public void rollback() throws SQLException {
		Transaction<?> transaction = getEnclosingTransaction();
		if (getEnclosingTransaction() != null) {
			transaction.rollback();
		} else {
			getEnclosingTransaction().commit();
		}
	}

	public void close() throws Exception {
		Transaction<?> transaction = getEnclosingTransaction();
		if (getEnclosingTransaction() != null) {
			transaction.close();
		}
	}
}
