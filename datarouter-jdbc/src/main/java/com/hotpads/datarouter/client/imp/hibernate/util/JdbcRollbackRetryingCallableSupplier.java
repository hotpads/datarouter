package com.hotpads.datarouter.client.imp.hibernate.util;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class JdbcRollbackRetryingCallableSupplier<T>
implements Supplier<JdbcRollbackRetryingCallable<T>>{
	
	private final Callable<T> callable;
	private final int numAttempts;
	private final long initialDoublingBackoffMs;
	
	public JdbcRollbackRetryingCallableSupplier(Callable<T> callable, int numAttempts,
			long initialDoublingBackoffMs){
		this.callable = callable;
		this.numAttempts = numAttempts;
		this.initialDoublingBackoffMs = initialDoublingBackoffMs;
	}

	@Override
	public JdbcRollbackRetryingCallable<T> get(){
		return new JdbcRollbackRetryingCallable<>(callable, numAttempts, initialDoublingBackoffMs);
	}
}