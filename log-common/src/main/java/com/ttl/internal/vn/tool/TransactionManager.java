package com.ttl.internal.vn.tool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;

public class TransactionManager
{
	private static final TransactionManager                 INSTANCE              = new TransactionManager();
	private static final ThreadLocal<Stack<Transaction<?>>> transactionStore = new ThreadLocal<>();
	private Datasource                                      datasource            = Datasource.getInstance();

	public abstract class ProxyTransaction<T> extends Transaction<T> {
		protected final Transaction<T> innerTransaction;

		public ProxyTransaction(Transaction<T> innerTransaction) {
			this.innerTransaction = innerTransaction;
		}

		@Override
		public abstract T runInTransaction(Transaction<?> outerTransaction) throws Exception;
		@Override
		public abstract void rollback() throws SQLException;
		@Override
		public abstract void commit() throws SQLException;
		@Override
		public abstract Transaction<?> getEnclosingTransaction();
	}

	public <T> T runTransaction(Transaction<T> transaction) throws Exception
	{
		Stack<Transaction<?>> transactionStack = transactionStackStore.get() != null ? transactionStackStore.get() : new Stack<>();
		transactionStackStore.set(transactionStack);
		Transaction<?> enclosingTransaction = transactionStack.peek();
		ProxyTransaction<T> proxyTransaction;
		if (enclosingTransaction == null) {
			proxyTransaction = new ProxyTransaction<T>(transaction)
			{
				private Connection inProgressConnection = datasource.getConnection();

				@Override
				public T runInTransaction() throws Exception
				{
					try
					{
						T result = innerTransaction.runInTransaction();
						commit();
						return result;
					} catch (Exception e) {
						rollback();
						throw new RuntimeException(e);
					} finally
					{
						close();
					}
				}

				@Override
				public Transaction<?> getEnclosingTransaction()
				{
					return null;
				}

				@Override
				public void commit() throws SQLException
				{
					if (inProgressConnection.isValid(0))
					{
						inProgressConnection.commit();
					}
				}

				@Override
				public void rollback() throws SQLException
				{
					if (inProgressConnection.isValid(0))
					{
						inProgressConnection.rollback();
					}
				}

				@Override
				public void close() throws SQLException
				{
					if (!inProgressConnection.isClosed())
					{
						transactionStackStore.remove();
						inProgressConnection.close();
					}
				}
			};
			transaction.setEnclosingTransaction(proxyTransaction);
			transactionStack.add(proxyTransaction);
		} else {
			proxyTransaction = new ProxyTransaction<>(transaction)
			{
				// NOTE: Not commit since enclosing transaction still on going
				@Override
				public T runInTransaction() throws Exception
				{
					try
					{
						return innerTransaction.runInTransaction();
					}
					catch (Exception e)
					{
						rollback();
						throw new RuntimeException(e);
					}
					finally
					{
						close();
					}
				}

				@Override
				public Transaction<?> getEnclosingTransaction()
				{
					return enclosingTransaction;
				}

				// NOTE: Do nothing as the enclosing transaction is still going
				@Override
				public void commit()
				{
				}

				@Override
				public void rollback() throws SQLException
				{
					getEnclosingTransaction().rollback();
				}

				@Override
				public void close()
				{
					transactionStack.pop();
				}
			};
			transaction.setEnclosingTransaction(proxyTransaction);
			proxyTransaction.setEnclosingTransaction(enclosingTransaction);
			transactionStack.add(proxyTransaction);
		}

		return proxyTransaction.runInTransaction();
	}

	public static TransactionManager getInstance() {
		return INSTANCE;
	}
}
