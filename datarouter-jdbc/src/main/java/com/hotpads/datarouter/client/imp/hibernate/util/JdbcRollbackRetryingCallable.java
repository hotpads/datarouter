package com.hotpads.datarouter.client.imp.hibernate.util;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.util.core.concurrent.ThreadTool;

/*
 * Catch both jdbc and jdbc4 rollback exceptions since they are both in the classpath somehow.
 */
public class JdbcRollbackRetryingCallable<T>
implements Callable<T>{
	private static final Logger logger = LoggerFactory.getLogger(JdbcRollbackRetryingCallable.class);

	
	private final Callable<T> callable;
	private final int numAttempts;
	private final long initialDoublingBackoffMs;

	
	public JdbcRollbackRetryingCallable(Callable<T> callable, int numAttempts, long initialDoublingBackoffMs){
		this.callable = callable;
		this.numAttempts = numAttempts;
		this.initialDoublingBackoffMs = initialDoublingBackoffMs;
	}


	@Override
	public T call() throws Exception{//throw non-RollbackExceptions directly
		long backoffMs = initialDoublingBackoffMs;
		for(int attemptNum = 1; attemptNum <= numAttempts; ++attemptNum){
			try{
				return callable.call();
			}catch(com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException
					| com.mysql.jdbc.exceptions.MySQLTransactionRollbackException rollbackException){
				if(attemptNum < numAttempts){
					logger.warn("rollback on attempt {}/{}, sleeping {}ms", attemptNum, numAttempts, backoffMs, 
							rollbackException);
					ThreadTool.sleep(backoffMs);
				}else{
					logger.error("rollback on final attempt {}", attemptNum, rollbackException);
					throw new RuntimeException(rollbackException);
				}
			}
			backoffMs *= 2;
		}
		throw new RuntimeException("shouldn't get here");
	}
	
}