package com.ttl.internal.vn.tool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;

public class TransactionManager
{
	private static final ThreadLocal<Stack<Transaction<?>>> transactionStore = new ThreadLocal<>();

	private final Datasource datasource;

	public TransactionManager(Datasource datasource) {
		this.datasource = datasource;
	}

	public abstract class ProxyTransaction<T> extends Transaction<T> {
		protected final Transaction<T> innerTransaction;

		public ProxyTransaction(Transaction<T> innerTransaction) {
			this.innerTransaction = innerTransaction;
		}

		@Override
		public T runInTransaction() throws Exception {
			try {
				innerTransaction.setEnclosingTransaction(this);
				T result = innerTransaction.runInTransaction();
				commit();
				return result;
			} catch (Exception e) {
				rollback();
				throw new RuntimeException(e);
			} finally {
				close();
			}
		}
	}

	public <T> T runTransaction(Transaction<T> transaction) throws Exception
	{
		Stack<Transaction<?>> transactionStack = transactionStore.get() != null ? transactionStore.get() : new Stack<>();
		transactionStore.set(transactionStack);
		Transaction<?> enclosingTransaction = transactionStack.peek();
		ProxyTransaction<T> proxyTransaction;
		if (enclosingTransaction == null) {
			proxyTransaction = new ProxyTransaction<>(transaction) {
                private Connection inProgressConnection = datasource.getConnection();

				@Override
				protected Connection getConnection() {
					return inProgressConnection;
				}

                @Override
                public void close() throws SQLException {
                    if (!inProgressConnection.isClosed()) {
                        inProgressConnection.close();
						transactionStore.remove();
					}
                }
            };
			transactionStack.add(proxyTransaction);
		} else {
			proxyTransaction = new ProxyTransaction<>(transaction)
			{
				@Override
				public void close() {
					transactionStack.pop();
				}
			};
			proxyTransaction.setEnclosingTransaction(enclosingTransaction);
			transactionStack.add(proxyTransaction);
		}

		return proxyTransaction.runInTransaction();
	}
}
