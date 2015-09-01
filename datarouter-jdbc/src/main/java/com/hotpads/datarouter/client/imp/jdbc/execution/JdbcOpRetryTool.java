package com.hotpads.datarouter.client.imp.jdbc.execution;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.util.core.concurrent.RetryableTool;

public class JdbcOpRetryTool{
	
	public static final int NUM_ROLLBACK_ATTEMPTS = 5;
	public static final long ROLLBACK_BACKOFF_MS = 4;
	
	//this defaults to 1, so you must explicitly call config.setNumAttempts(x) to get retries on 
	// non-MySQLTransactionRollbackExceptions
	public static final int DEFAULT_NUM_ATTEMPTS = 1;
	public static final long DEFAULT_BACKOFF_MS = 1;

	
	/* This executes the query config.getNumAttempts() times. Then for each attempt, it will retry rollbacks a fixed
	 * number of times (NUM_ROLLBACK_ATTEMPTS - 1). If config.getNumAttempts() is 2 and NUM_ROLLBACK_ATTEMPTS is 3, then
	 * we may start 6 txns */
	public static <T> T tryNTimes(SessionExecutorImpl<T> opCallable, Config config){
		config = Config.nullSafe(config);
		JdbcRollbackRetryingCallable<T> retryingCallable = new JdbcRollbackRetryingCallable<>(opCallable,
				NUM_ROLLBACK_ATTEMPTS, ROLLBACK_BACKOFF_MS);
		int numAttempts = config.getNumAttemptsOrUse(DEFAULT_NUM_ATTEMPTS);
		return RetryableTool.tryNTimesWithBackoffUnchecked(retryingCallable, numAttempts, DEFAULT_BACKOFF_MS);
	}

}
